#!flask/bin/python
import logging
import codecs
import requests
import feedparser
import json
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
def get_media_popularity_score(self, candidate_name):
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

candidate_analysis_df = create_df("%s%s" % ("../",cfg["OUTPUT_DATA_SRC"]["CANDIDATE_ANALYSED_LIST"]['CSV']))


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
        content = request.json
        return content


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