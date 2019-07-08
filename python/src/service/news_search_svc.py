#!flask/bin/python
import logging
import codecs
import requests
import feedparser
import json
from flask import Flask
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


logging.basicConfig(format='%(asctime)s - %(message)s', level=logging.INFO)

cfg = get_config("../../config/cfg.json")


class NewsSearch(Resource):

    request_args = {"candidateName": fields.Str(missing="Modi")}

    @lru_cache(maxsize=3000)
    def get_media_popularity_score(self, candidate_name):
        response = requests.get(cfg["NEWS_URL"], params={"q": '"%s"' % candidate_name,
                                                         "hl": "en-SG", "gl": "SG", "ceid": "SG:en"},
                                proxies=cfg['PROXY'])

        if response.status_code == 200:

            d = feedparser.parse(response.text)

            logging.info("Name : %s, Number Of Items Found : %d" % (candidate_name, len(d['entries'])))

            return {"numberOfNewsItems": len(d['entries'])}
        else:
            return {"numberOfNewsItems": 0.000}

    @use_args(request_args)
    def get(self, args):
        return self.get_media_popularity_score(args["candidateName"])


app = Flask(__name__)
api = Api(app)
CORS(app, resources={r"/news/*": {"origins": "*"}})

if __name__ == '__main__':
    api.add_resource(NewsSearch, '/news/search')
    app.run(debug=True)