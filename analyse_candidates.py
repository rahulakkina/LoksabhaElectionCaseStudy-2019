import logging

import pandas as pd
import numpy as np

logging.basicConfig(format='%(asctime)s - %(message)s', level=logging.INFO)

'''Utility Class with few utility methods'''
class ElectionUtils(object):
    '''Extracts CSV contents to a dataframe'''
    def create_df(self, file_name):
        return pd.read_csv(file_name, header='infer')

    '''Predicts what a person should be earning given the age bracket (Note : More accurate data in affidavits shall help predicting it better)'''
    def age_earning_df(self, age_idx_df, candidates_data_df):
        for index, row in age_idx_df.iterrows():
            cd_df = candidates_data_df[
                (candidates_data_df['AGE'] >= row['FROM']) & (candidates_data_df['AGE'] <= row['TO'])]

            #Earnings calculated by the formula EARNINGS = (MOVABLE ASSETS + IMMOVABLE ASSETS) - LIABLITIES
            mean_earnings = ((cd_df['MOVABLE_ASSETS'] + cd_df['IMMOVABLE_ASSETS']) - cd_df['LIABLITIES']).mean()

            #Calculating mean earnings
            age_idx_df.loc[index, 'AVERAGE_EARNINGS'] = mean_earnings

            #Taking boundaries of 50% average for earnings evaluation
            age_idx_df.loc[index, 'MINIMUM_EARNINGS'] = mean_earnings - (mean_earnings * 0.5)
            age_idx_df.loc[index, 'MAXIMUM_EARNINGS'] = mean_earnings + (mean_earnings * 0.5)

        return age_idx_df



class CandidateDataTransformation(object):
    # Initilization
    utils = ElectionUtils()
    [candidates_data_df, education_idx_df, weights_df, candidate_analysis_df] = [utils.create_df("datasets/CANDIDATES_LIST.csv"),
                                                          utils.create_df("datasets/EDUCATION_INDEX.csv"),
                                                          utils.create_df("datasets/WEIGHTAGE.csv"), utils.create_df("datasets/CANDIDATE_ANALYSED_LIST.csv")]
    age_idx_df = utils.age_earning_df(utils.create_df("datasets/AGE_INDEX.csv"), candidates_data_df)


    '''Returns points which can be earned given the age bracket (Please note older people score less through this system)'''
    def get_age_related_points(self, age):
        for index, row in self.age_idx_df.iterrows():
            if row['FROM'] <= age and row['TO'] >= age:
                return row['POINTS']

    '''Returns points w.r.t number of criminal cases'''
    def get_criminal_case_points(self, pending_cases, convicted_cases):
        # If convicted we get a negative score twice that of offenses one has committed
        if convicted_cases > 0:
            return -2 * convicted_cases
        # we get a negative score depending on no. of offenses committed
        elif pending_cases > 0:
            return -1 * pending_cases
        else:
            return 0

    '''Returns points calculated based on education'''
    def get_edu_points(self, education):
        return self.education_idx_df[self.education_idx_df['EDUCATION'] == education]['POINTS'].values[0]

    def get_edu_from_points(self, points):
        for index, row in self.education_idx_df.iterrows():
            if row['FROM'] == int(points):
                return row['EDUCATION']
        return 'Literate'

    '''Returns points based on income tax compliance, if you are a regular payer you earn a point'''
    def get_tax_compliance_points(self, tax_compliance):
        dict = {"YES": 1, "NO": 0}
        return dict[tax_compliance]

    '''Returns points based on government dues payout compliance, if you are a regular payer you earn a point'''
    def get_govt_due_points(self, govt_due):
        dict = {"YES": 0, "NO": 1}
        return dict[govt_due]

    '''Returns points based on residency, if the contestent is a local he/she gets a point'''
    def get_local_residency_points(self, local_residency):
        dict = {"YES": 1, "NO": 0}
        return dict[local_residency]

    '''Returns points based on earnings, if he/she earns less or more than average distribution they'd get a 0'''
    def get_earnings_points(self, age, earnings):

        for index, row in self.age_idx_df.iterrows():
            if row['FROM'] <= age and row['TO'] >= age:
                if row['MAXIMUM_EARNINGS'] < earnings or row['MINIMUM_EARNINGS'] > earnings:
                    return 0
                else:
                    return 1

    '''Gets configured weight'''
    def get_weight(self, key):
        return self.weights_df[self.weights_df['KEY'] == key]['WEIGHT'].values[0]

    '''Evaluates all contestants based on the ratings system and returns a dataframe which is later exported into a CSV for report'''
    def evaluate(self):

        for index, row in self.candidates_data_df.iterrows():
            [age_related_points, criminal_case_points, edu_points, tax_compliance_points, govt_due_points,
             local_residency_points, earnings_points] = \
                [self.get_age_related_points(row['AGE']),
                 self.get_criminal_case_points(row['PENDING_CRIMINAL_CASES'], row['CONVICTED_CRIMINAL_CASES']),
                 self.get_edu_points(row['EDUCATION']), self.get_tax_compliance_points(row['INCOME_TAX_COMPLIANCE']),
                 self.get_govt_due_points(row['GOVERNMENT_DUES']),
                 self.get_local_residency_points(row['LOCAL_RESIDENT']),
                 self.get_earnings_points(row['AGE'],
                                          ((row['MOVABLE_ASSETS'] + row['IMMOVABLE_ASSETS']) - row['LIABLITIES']))]

            # Weighted sum of all the declared details.
            points_earned = (self.get_weight("AGE") * age_related_points) + (
                self.get_weight("CRIMINAL_CASES") * criminal_case_points) + (
                                self.get_weight("EDUCATION") * edu_points) + (
                                self.get_weight("INCOME_TAX_COMPLIANCE") * tax_compliance_points) + (
                                self.get_weight("GOVERNMENT_DUES") * govt_due_points) + (
                                self.get_weight("LOCAL_RESIDENT") * local_residency_points) + (
                                self.get_weight("EARNINGS") * earnings_points)

            self.candidates_data_df.loc[index, 'POINTS_FROM_AGE'] = age_related_points
            self.candidates_data_df.loc[index, 'POINTS_FROM_CRIMINAL_CASES'] = criminal_case_points
            self.candidates_data_df.loc[index, 'POINTS_FROM_EDUCATION'] = edu_points
            self.candidates_data_df.loc[index, 'POINTS_FROM_INCOME_TAX_COMPLIANCE'] = tax_compliance_points
            self.candidates_data_df.loc[index, 'POINTS_FROM_GOVT_DUE_COMPLIANCE'] = govt_due_points
            self.candidates_data_df.loc[index, 'POINTS_FROM_LOCAL_RESIDENCY'] = local_residency_points
            self.candidates_data_df.loc[index, 'POINTS_FROM_EARNINGS'] = earnings_points
            self.candidates_data_df.loc[index, 'POINTS_EARNED'] = points_earned

            logging.debug("%s, %d, %d, %d, %d, %d, %d, %d, %d" % (
                row['NAME'], age_related_points, criminal_case_points, edu_points, tax_compliance_points,
                govt_due_points,
                local_residency_points, earnings_points, points_earned))

        return self.candidates_data_df

    def calculate_party_criminal_score(self):
        ca_df = self.candidate_analysis_df.groupby(['PARTY']).agg({'CANDIDATE_NAME' : 'count', 'NO_PENDING_CRIMINAL_CASES' : 'sum'}).reset_index().rename(columns={'CANDIDATE_NAME':'NO_CONTESTING_CANDIDATES'})
        ca_df['PENDING_CRIMINAL_CASES_PER_CANDIDATE'] = round((ca_df['NO_PENDING_CRIMINAL_CASES'] / ca_df['NO_CONTESTING_CANDIDATES']),2)
        return ca_df

    def build_candidate_analysis_df(self, df):
        for index, row in df.iterrows():
            df.loc[index, 'POINTS_FOR_EDUCATION'] = self.get_edu_points(row['EDUCATION'])
        return df

    def calculate_party_education_score(self):
        edu_df = self.build_candidate_analysis_df(self.candidate_analysis_df)
        edu_df = edu_df.groupby(['PARTY']).agg({'POINTS_FOR_EDUCATION': 'mean'}).reset_index().rename(columns={'POINTS_FOR_EDUCATION':'EDUCATION_INDEX'})
        edu_df['EDUCATION_INDEX'] = round(edu_df['EDUCATION_INDEX'])
        return edu_df

# Main Section
cdt = CandidateDataTransformation()

export_df = cdt.evaluate()

export_df.to_csv("datasets/CONTESTANT_LIST.csv", index=False, header=True)

json_content = export_df.to_json('datasets/CONTESTANT_LIST.json', orient='records')

logging.info("Exported %d rows to %s file" % (len(export_df), "datasets/CONTESTANT_LIST.csv"))

# Aggregating analysis for party wise candidate recuirtment based on criminal history.

cc_df = cdt.calculate_party_criminal_score()

cc_df.to_csv("datasets/PENDING_CRIMINAL_CASES_BY_PARTY.csv", index=False, header=True)

logging.info("Exported %d rows to %s file" % (len(cc_df), "datasets/PENDING_CRIMINAL_CASES_BY_PARTY.csv"))

edu_df = cdt.calculate_party_education_score()

edu_df.to_csv("datasets/EDUCATION_INDEX_BY_PARTY.csv", index=False, header=True)

logging.info("Exported %d rows to %s file" % (len(edu_df), "datasets/EDUCATION_INDEX_BY_PARTY.csv"))







