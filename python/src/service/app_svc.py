#!flask/bin/python
import logging
import codecs
import requests
import feedparser
import json
import xgboost
import pandas as pd
from flask import Flask, request
from flask_restful import Api, Resource
from webargs import fields
from webargs.flaskparser import use_args
from flask_cors import CORS
from functools import lru_cache


# Loads json configuration from the configuration file.


def get_config(conf_path):
    with codecs.open(conf_path, 'r', 'utf-8-sig') as json_data:
        d = json.load(json_data)
        return d


def create_df(file_name):
    return pd.read_csv(file_name, header='infer')


@lru_cache(maxsize=3000)
def get_media_popularity_score(candidate_name):
    response = requests.get(cfg["NEWS_URL"], params={"q": '"%s"' % candidate_name,
                                             "hl": "en-SG", "gl": "SG", "ceid": "SG:en"}, proxies=cfg['PROXY'])
    if response.status_code == 200:
        d = feedparser.parse(response.text)
        logging.info("Name : %s, Number Of Items Found : %d" % (candidate_name, len(d['entries'])))
        return len(d['entries'])
    else:
        return 0.000


logging.basicConfig(format='%(asctime)s - %(message)s', level=logging.INFO)

cfg = get_config("../../config/cfg.json")

age_df = create_df("%s%s" % ("../", cfg['INPUT_DATA_SRC']['AGE_INDEX']))

candidate_analysis_df = create_df("%s%s" % ("../", cfg["OUTPUT_DATA_SRC"]["CANDIDATE_ANALYSED_LIST"]['CSV']))

xgb = xgboost.XGBRegressor()
xgb.load_model("%s%s" % ("../", cfg["ML"]["MODEL"]))


def get_age_related_points(age):
    for index, row in age_df.iterrows():
        if row['FROM'] <= age <= row['TO']:
            return row['POINTS']
    return 1


def build_age_earnings_dict():
    dic = {}
    for idx, r in candidate_analysis_df.iterrows():
        age_index = r["AGE_GROUP_IDX"]
        earnings = r["EARNINGS"]
        if age_index not in dic.keys():
            dic[age_index] = [0, 0]
        if earnings > 1:
            dic[age_index] = [dic[age_index][0] + 1, dic[age_index][1] + earnings]
    return dic


age_earnings_dict = build_age_earnings_dict()


'''Gets info about states'''


class StateInfo(Resource):

    state_df = create_df("%s%s" % ("../", cfg['INPUT_DATA_SRC']['STATES_INDEX']))

    def get(self):
        return self.state_df.to_dict('records')


'''Get constituencies listed under given state'''


class ConstituenciesInfo(Resource):
    request_args = {"stateName": fields.Str(missing="ANDHRA PRADESH")}
    constituency_df = create_df("%s%s" % ("../", cfg['INPUT_DATA_SRC']['CONSTITUENCIES']))

    @use_args(request_args)
    def get(self, args):
        return self.constituency_df[self.constituency_df["STATE"] == args["stateName"]].to_dict('records')


'''Gets details around all contesting political parties'''


class PoliticalPartiesInfo(Resource):
    party_df = create_df("%s%s" % ("../", cfg['INPUT_DATA_SRC']['POLITICAL_PARTY_INDEX']))

    def get(self):
        return self.party_df.to_dict('records')


'''Gets details of candidates contesting  the election'''


class CandidatesInfo(Resource):

    request_args = {"candidateName": fields.Str(missing="")}

    @use_args(request_args)
    def get(self, args):
        return \
            candidate_analysis_df[
                candidate_analysis_df["CANDIDATE_NAME"].str
                    .lower().str.contains(args["candidateName"], case=False)].to_dict('records')


'''Gets education listings and their ratings'''


class EducationInfo(Resource):
    education_df = create_df("%s%s" % ("../", cfg['INPUT_DATA_SRC']['EDUCATION_INDEX']))

    def get(self):
        return self.education_df.to_dict('records')


class Predictor(Resource):

    def post(self):
        payload = request.json
        input_vector = self.build_input_vector(payload)
        score = xgb.predict(input_vector)
        return {"score": round(float(score[0]), 4)}

    def build_input_vector(self, payload):

        age_points = get_age_related_points(payload["age"])
        constituency_id = payload["constituencyId"]
        state_id = payload["stateId"]
        party_id = payload["partyId"]
        no_pen_crime_cases = payload["numberOfPendingCriminalCases"]

        [t, s] = age_earnings_dict[age_points]
        earnings_average = round(s / t, 2)
        earned_income = payload["earnedIncome"] - payload["liabilities"]
        [limit_min, limit_max] = [(earnings_average - (earnings_average * 0.5)),
                                  (earnings_average + (earnings_average * 0.5))]

        earnings_points = 1 if limit_min <= earned_income <= limit_max else 0

        state_literacy_rate = round(payload["stateLiteracyRate"], 4)
        state_seat_share = round(payload["stateSeatShare"], 4)
        party_group_id = payload["partyGroupId"]
        education_group_id = payload["educationGroupId"]
        delta_voter_turnout = payload["deltaStateVoterTurnout"]
        number_of_phases = payload["numberOfPhases"]
        re_contest = 1 if payload["recontest"] else 0
        sex = 1 if payload["sex"] else 0
        media_popularity = round(get_media_popularity_score(payload["candidateName"]) / 100.0, 2)

        return [[age_points, constituency_id, state_id, party_id, no_pen_crime_cases, earnings_points,
                    state_literacy_rate, state_seat_share, party_group_id, education_group_id, delta_voter_turnout,
                        number_of_phases, re_contest, sex, media_popularity]]


app = Flask(__name__)
api = Api(app)
CORS(app, resources={r"/loksabhaElections/*": {"origins": "*"}})


if __name__ == '__main__':
    api.add_resource(EducationInfo, '/loksabhaElections/educationInfo')
    api.add_resource(StateInfo, '/loksabhaElections/statesInfo')
    api.add_resource(ConstituenciesInfo, '/loksabhaElections/constituenciesInfo')
    api.add_resource(PoliticalPartiesInfo, '/loksabhaElections/politicalPartiesInfo')
    api.add_resource(CandidatesInfo, '/loksabhaElections/candidatesInfo')
    api.add_resource(Predictor, "/loksabhaElections/predict")
    app.run(debug=True)