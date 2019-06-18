import pandas as pd
import logging
import codecs
import json
import numpy as np
from sklearn.metrics import mean_squared_error
from sklearn.model_selection import train_test_split
from sklearn.model_selection import cross_val_score
import xgboost
import warnings

# Loads json configuration from the configuration file.


def get_config(conf_path):
    with codecs.open(conf_path, 'r', 'utf-8-sig') as json_data:
        d = json.load(json_data)
        return d


logging.basicConfig(format='%(asctime)s - %(message)s', level=logging.INFO)

warnings.filterwarnings("ignore")

cfg = get_config("config/cfg.json")

df = pd.read_csv(cfg["ML"]["TRAIN"], header='infer')

logging.info("Dataset Dimenstions - Rows : %d, Columns : %d" % (df.shape[0],  df.shape[1]))

x = df.iloc[:, 2:17]
y = df.iloc[:, 17]

X = x.values
Y = y.values


# Splitting the dataset into the Training set and Test set

logging.info("Splitting data set for Cross Validation ..")

X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size=0.25, random_state=2)


# Fitting XGBoost to the Training set

xgb = xgboost.XGBRegressor(colsample_bytree=0.8, subsample=0.5, learning_rate=0.02, max_depth=10,
                           min_child_weight=1, n_estimators=5000, reg_alpha=0.1, reg_lambda=0.2,
                           gamma=0.01, silent=True, random_state=7, nthread=-1, missing=None, booster="dart")

logging.info("Fitting XGBoost to the training set ...")

xgb.fit(X_train, Y_train, eval_metric='logloss', verbose=True)


# Predicting the Test set results

logging.info("Predicting the Test set results ....")

Y_pred = xgb.predict(X_test)

Y_train_pred = xgb.predict(X_train)

xgb.save_model(cfg["ML"]["MODEL"])

# Applying k-Fold Cross Validation

logging.info("Applying K-Fold Cross Validation .....")

accuracies = cross_val_score(estimator=xgb, X=X_train, y=Y_train, cv=10, n_jobs=2, pre_dispatch=4)

logging.info("Accuracies - Mean : %f, Standard Deviation : %f" % (accuracies.mean(), accuracies.std()))

logging.info("Calculating Root Mean Square Error ......")

RMSE = np.sqrt(mean_squared_error(Y_train, Y_train_pred))

logging.info("Train - RMSE : %f" % RMSE.round(4))

RMSE = np.sqrt(mean_squared_error(Y_test, Y_pred))

logging.info("Test - RMSE : %f" % RMSE.round(4))

xgb.save_model(cfg["ML"]["MODEL"])