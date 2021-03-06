import xgboost


data = [[1,456,34,187,0,0,0.6972,0.147,3,2,0.0121,7,0,0,0.1]]


xgb = xgboost.XGBRegressor(colsample_bytree=0.8, subsample=0.5, learning_rate=0.01, max_depth=12,
                           min_child_weight=1, n_estimators=5000, reg_alpha=0.1, reg_lambda=0.2,
                           gamma=0.01, silent=True, random_state=7, nthread=-1, missing=None, booster="dart")

xgb.load_model("../../datasets/model/LS-2019-prediction.model")

y = xgb.predict(data)

print(y)

