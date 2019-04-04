import pandas as pd
import logging

logging.basicConfig(format='%(asctime)s - %(message)s', level=logging.INFO)

class ElectionUtils(object):

    def create_df(self, file_name):
        return pd.read_csv(file_name, header='infer')

    def age_earning_df(self, age_idx_df, candidates_data_df):
            for index, row in age_idx_df.iterrows():
                cd_df = candidates_data_df[
                    (candidates_data_df['AGE'] >= row['FROM']) & (candidates_data_df['AGE'] <= row['TO'])]
                mean_earnings = ((cd_df['MOVABLE_ASSETS'] + cd_df['IMMOVABLE_ASSETS']) - cd_df['LIABLITIES']).mean()
                age_idx_df.loc[index, 'AVERAGE_EARNINGS'] = mean_earnings
                age_idx_df.loc[index, 'MINIMUM_EARNINGS'] = mean_earnings - (mean_earnings * 0.5)
                age_idx_df.loc[index, 'MAXIMUM_EARNINGS'] = mean_earnings + (mean_earnings * 0.5)
            return age_idx_df



class CandidateDataTransformation(object):

    utils = ElectionUtils()
    [candidates_data_df, education_idx_df, weights_df] = [utils.create_df("datasets/CANDIDATES_LIST.csv"), utils.create_df("datasets/EDUCATION_INDEX.csv"), utils.create_df("datasets/WEIGHTAGE.csv")]
    age_idx_df = utils.age_earning_df(utils.create_df("datasets/AGE_INDEX.csv"), candidates_data_df)


    def get_age_related_points(self, age):
        for index, row in self.age_idx_df.iterrows():
            if row['FROM'] <= age and row['TO'] >= age:
                return row['POINTS']

    def get_criminal_case_points(self, pending_cases, convicted_cases):
        if convicted_cases > 0:
            return -2 * convicted_cases
        elif pending_cases > 0:
            return -1 * pending_cases
        else:
            return 0

    def get_edu_points(self, education):
        return self.education_idx_df[self.education_idx_df['EDUCATION'] == education]['POINTS'].values[0]

    def get_tax_compliance_points(self, tax_compliance):
        dict = {"YES": 1, "NO": 0}
        return dict[tax_compliance]

    def get_govt_due_points(self, govt_due):
        dict = {"YES": 0, "NO": 1}
        return dict[govt_due]

    def get_local_residency_points(self, local_residency):
        dict = {"YES": 1, "NO": 0}
        return dict[local_residency]

    def get_earnings_points(self, age, earnings):

        for index, row in self.age_idx_df.iterrows():
            if row['FROM'] <= age and row['TO'] >= age:
                if row['MAXIMUM_EARNINGS'] < earnings or row['MINIMUM_EARNINGS'] > earnings:
                    return 0
                else:
                    return 1

    def get_weight(self, key):
        return self.weights_df[self.weights_df['KEY'] == key]['WEIGHT'].values[0]

    def evaluate(self):

        for index, row in self.candidates_data_df.iterrows():

            [age_related_points, criminal_case_points, edu_points, tax_compliance_points, govt_due_points, local_residency_points, earnings_points] = \
                                                        [self.get_age_related_points(row['AGE']),
                                                         self.get_criminal_case_points(row['PENDING_CRIMINAL_CASES'],  row['CONVICTED_CRIMINAL_CASES']),
                                                         self.get_edu_points(row['EDUCATION']), self.get_tax_compliance_points(row['INCOME_TAX_COMPLIANCE']),
                                                         self.get_govt_due_points(row['GOVERNMENT_DUES']), self.get_local_residency_points(row['LOCAL_RESIDENT']),
                                                         self.get_earnings_points(row['AGE'], ((row['MOVABLE_ASSETS'] + row['IMMOVABLE_ASSETS']) - row['LIABLITIES']))]


            points_earned = (self.get_weight("AGE") *  age_related_points) + (self.get_weight("CRIMINAL_CASES") *  criminal_case_points) + (self.get_weight("EDUCATION") *  edu_points) + (self.get_weight("INCOME_TAX_COMPLIANCE") *  tax_compliance_points) + (self.get_weight("GOVERNMENT_DUES") *  govt_due_points) + (self.get_weight("LOCAL_RESIDENT") *  local_residency_points) + (self.get_weight("EARNINGS") *  earnings_points)

            self.candidates_data_df.loc[index, 'POINTS_FROM_AGE'] = age_related_points
            self.candidates_data_df.loc[index, 'POINTS_FROM_CRIMINAL_CASES'] = criminal_case_points
            self.candidates_data_df.loc[index, 'POINTS_FROM_EDUCATION'] = edu_points
            self.candidates_data_df.loc[index, 'POINTS_FROM_INCOME_TAX_COMPLIANCE'] = tax_compliance_points
            self.candidates_data_df.loc[index, 'POINTS_FROM_GOVT_DUE_COMPLIANCE'] = govt_due_points
            self.candidates_data_df.loc[index, 'POINTS_FROM_LOCAL_RESIDENCY'] = local_residency_points
            self.candidates_data_df.loc[index, 'POINTS_FROM_EARNINGS'] = earnings_points
            self.candidates_data_df.loc[index, 'POINTS_EARNED'] = points_earned

            logging.debug("%s, %d, %d, %d, %d, %d, %d, %d, %d"%(row['NAME'], age_related_points, criminal_case_points, edu_points, tax_compliance_points, govt_due_points, local_residency_points, earnings_points, points_earned))

        return self.candidates_data_df




export_df = CandidateDataTransformation().evaluate()
export_df.to_csv("datasets/CONTESTANT_LIST.csv", index=False, header=True)
logging.info("Exported %d rows to %s file"%(len(export_df), "datasets/CONTESTANT_LIST.csv"))