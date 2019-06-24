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

data = [[2,414,32,187,0,1,0.665,0.031,3,0,-0.0814,1,0,0,0.01]]
##0.4733, 0.6537


xgb = xgboost.XGBRegressor(colsample_bytree=0.8, subsample=0.5, learning_rate=0.01, max_depth=12,
                           min_child_weight=1, n_estimators=5000, reg_alpha=0.1, reg_lambda=0.2,
                           gamma=0.01, silent=True, random_state=7, nthread=-1, missing=None, booster="dart")

xgb.load_model("datasets/model/LS-2019-prediction.model")

y = xgb.predict(data)

print(y)

