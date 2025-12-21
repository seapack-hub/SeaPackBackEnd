package org.seaPack.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class NavDataExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public NavDataExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        protected void addCriterionForJDBCDate(String condition, Date value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value.getTime()), property);
        }

        protected void addCriterionForJDBCDate(String condition, List<Date> values, String property) {
            if (values == null || values.size() == 0) {
                throw new RuntimeException("Value list for " + property + " cannot be null or empty");
            }
            List<java.sql.Date> dateList = new ArrayList<>();
            Iterator<Date> iter = values.iterator();
            while (iter.hasNext()) {
                dateList.add(new java.sql.Date(iter.next().getTime()));
            }
            addCriterion(condition, dateList, property);
        }

        protected void addCriterionForJDBCDate(String condition, Date value1, Date value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value1.getTime()), new java.sql.Date(value2.getTime()), property);
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andFundCodeIsNull() {
            addCriterion("fund_code is null");
            return (Criteria) this;
        }

        public Criteria andFundCodeIsNotNull() {
            addCriterion("fund_code is not null");
            return (Criteria) this;
        }

        public Criteria andFundCodeEqualTo(String value) {
            addCriterion("fund_code =", value, "fundCode");
            return (Criteria) this;
        }

        public Criteria andFundCodeNotEqualTo(String value) {
            addCriterion("fund_code <>", value, "fundCode");
            return (Criteria) this;
        }

        public Criteria andFundCodeGreaterThan(String value) {
            addCriterion("fund_code >", value, "fundCode");
            return (Criteria) this;
        }

        public Criteria andFundCodeGreaterThanOrEqualTo(String value) {
            addCriterion("fund_code >=", value, "fundCode");
            return (Criteria) this;
        }

        public Criteria andFundCodeLessThan(String value) {
            addCriterion("fund_code <", value, "fundCode");
            return (Criteria) this;
        }

        public Criteria andFundCodeLessThanOrEqualTo(String value) {
            addCriterion("fund_code <=", value, "fundCode");
            return (Criteria) this;
        }

        public Criteria andFundCodeLike(String value) {
            addCriterion("fund_code like", value, "fundCode");
            return (Criteria) this;
        }

        public Criteria andFundCodeNotLike(String value) {
            addCriterion("fund_code not like", value, "fundCode");
            return (Criteria) this;
        }

        public Criteria andFundCodeIn(List<String> values) {
            addCriterion("fund_code in", values, "fundCode");
            return (Criteria) this;
        }

        public Criteria andFundCodeNotIn(List<String> values) {
            addCriterion("fund_code not in", values, "fundCode");
            return (Criteria) this;
        }

        public Criteria andFundCodeBetween(String value1, String value2) {
            addCriterion("fund_code between", value1, value2, "fundCode");
            return (Criteria) this;
        }

        public Criteria andFundCodeNotBetween(String value1, String value2) {
            addCriterion("fund_code not between", value1, value2, "fundCode");
            return (Criteria) this;
        }

        public Criteria andNetAssetValueIsNull() {
            addCriterion("net_asset_value is null");
            return (Criteria) this;
        }

        public Criteria andNetAssetValueIsNotNull() {
            addCriterion("net_asset_value is not null");
            return (Criteria) this;
        }

        public Criteria andNetAssetValueEqualTo(BigDecimal value) {
            addCriterion("net_asset_value =", value, "netAssetValue");
            return (Criteria) this;
        }

        public Criteria andNetAssetValueNotEqualTo(BigDecimal value) {
            addCriterion("net_asset_value <>", value, "netAssetValue");
            return (Criteria) this;
        }

        public Criteria andNetAssetValueGreaterThan(BigDecimal value) {
            addCriterion("net_asset_value >", value, "netAssetValue");
            return (Criteria) this;
        }

        public Criteria andNetAssetValueGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("net_asset_value >=", value, "netAssetValue");
            return (Criteria) this;
        }

        public Criteria andNetAssetValueLessThan(BigDecimal value) {
            addCriterion("net_asset_value <", value, "netAssetValue");
            return (Criteria) this;
        }

        public Criteria andNetAssetValueLessThanOrEqualTo(BigDecimal value) {
            addCriterion("net_asset_value <=", value, "netAssetValue");
            return (Criteria) this;
        }

        public Criteria andNetAssetValueIn(List<BigDecimal> values) {
            addCriterion("net_asset_value in", values, "netAssetValue");
            return (Criteria) this;
        }

        public Criteria andNetAssetValueNotIn(List<BigDecimal> values) {
            addCriterion("net_asset_value not in", values, "netAssetValue");
            return (Criteria) this;
        }

        public Criteria andNetAssetValueBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("net_asset_value between", value1, value2, "netAssetValue");
            return (Criteria) this;
        }

        public Criteria andNetAssetValueNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("net_asset_value not between", value1, value2, "netAssetValue");
            return (Criteria) this;
        }

        public Criteria andAccumulatedNavIsNull() {
            addCriterion("accumulated_nav is null");
            return (Criteria) this;
        }

        public Criteria andAccumulatedNavIsNotNull() {
            addCriterion("accumulated_nav is not null");
            return (Criteria) this;
        }

        public Criteria andAccumulatedNavEqualTo(BigDecimal value) {
            addCriterion("accumulated_nav =", value, "accumulatedNav");
            return (Criteria) this;
        }

        public Criteria andAccumulatedNavNotEqualTo(BigDecimal value) {
            addCriterion("accumulated_nav <>", value, "accumulatedNav");
            return (Criteria) this;
        }

        public Criteria andAccumulatedNavGreaterThan(BigDecimal value) {
            addCriterion("accumulated_nav >", value, "accumulatedNav");
            return (Criteria) this;
        }

        public Criteria andAccumulatedNavGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("accumulated_nav >=", value, "accumulatedNav");
            return (Criteria) this;
        }

        public Criteria andAccumulatedNavLessThan(BigDecimal value) {
            addCriterion("accumulated_nav <", value, "accumulatedNav");
            return (Criteria) this;
        }

        public Criteria andAccumulatedNavLessThanOrEqualTo(BigDecimal value) {
            addCriterion("accumulated_nav <=", value, "accumulatedNav");
            return (Criteria) this;
        }

        public Criteria andAccumulatedNavIn(List<BigDecimal> values) {
            addCriterion("accumulated_nav in", values, "accumulatedNav");
            return (Criteria) this;
        }

        public Criteria andAccumulatedNavNotIn(List<BigDecimal> values) {
            addCriterion("accumulated_nav not in", values, "accumulatedNav");
            return (Criteria) this;
        }

        public Criteria andAccumulatedNavBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("accumulated_nav between", value1, value2, "accumulatedNav");
            return (Criteria) this;
        }

        public Criteria andAccumulatedNavNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("accumulated_nav not between", value1, value2, "accumulatedNav");
            return (Criteria) this;
        }

        public Criteria andAdjustedNavIsNull() {
            addCriterion("adjusted_nav is null");
            return (Criteria) this;
        }

        public Criteria andAdjustedNavIsNotNull() {
            addCriterion("adjusted_nav is not null");
            return (Criteria) this;
        }

        public Criteria andAdjustedNavEqualTo(BigDecimal value) {
            addCriterion("adjusted_nav =", value, "adjustedNav");
            return (Criteria) this;
        }

        public Criteria andAdjustedNavNotEqualTo(BigDecimal value) {
            addCriterion("adjusted_nav <>", value, "adjustedNav");
            return (Criteria) this;
        }

        public Criteria andAdjustedNavGreaterThan(BigDecimal value) {
            addCriterion("adjusted_nav >", value, "adjustedNav");
            return (Criteria) this;
        }

        public Criteria andAdjustedNavGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("adjusted_nav >=", value, "adjustedNav");
            return (Criteria) this;
        }

        public Criteria andAdjustedNavLessThan(BigDecimal value) {
            addCriterion("adjusted_nav <", value, "adjustedNav");
            return (Criteria) this;
        }

        public Criteria andAdjustedNavLessThanOrEqualTo(BigDecimal value) {
            addCriterion("adjusted_nav <=", value, "adjustedNav");
            return (Criteria) this;
        }

        public Criteria andAdjustedNavIn(List<BigDecimal> values) {
            addCriterion("adjusted_nav in", values, "adjustedNav");
            return (Criteria) this;
        }

        public Criteria andAdjustedNavNotIn(List<BigDecimal> values) {
            addCriterion("adjusted_nav not in", values, "adjustedNav");
            return (Criteria) this;
        }

        public Criteria andAdjustedNavBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("adjusted_nav between", value1, value2, "adjustedNav");
            return (Criteria) this;
        }

        public Criteria andAdjustedNavNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("adjusted_nav not between", value1, value2, "adjustedNav");
            return (Criteria) this;
        }

        public Criteria andNavDateIsNull() {
            addCriterion("nav_date is null");
            return (Criteria) this;
        }

        public Criteria andNavDateIsNotNull() {
            addCriterion("nav_date is not null");
            return (Criteria) this;
        }

        public Criteria andNavDateEqualTo(Date value) {
            addCriterionForJDBCDate("nav_date =", value, "navDate");
            return (Criteria) this;
        }

        public Criteria andNavDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("nav_date <>", value, "navDate");
            return (Criteria) this;
        }

        public Criteria andNavDateGreaterThan(Date value) {
            addCriterionForJDBCDate("nav_date >", value, "navDate");
            return (Criteria) this;
        }

        public Criteria andNavDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("nav_date >=", value, "navDate");
            return (Criteria) this;
        }

        public Criteria andNavDateLessThan(Date value) {
            addCriterionForJDBCDate("nav_date <", value, "navDate");
            return (Criteria) this;
        }

        public Criteria andNavDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("nav_date <=", value, "navDate");
            return (Criteria) this;
        }

        public Criteria andNavDateIn(List<Date> values) {
            addCriterionForJDBCDate("nav_date in", values, "navDate");
            return (Criteria) this;
        }

        public Criteria andNavDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("nav_date not in", values, "navDate");
            return (Criteria) this;
        }

        public Criteria andNavDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("nav_date between", value1, value2, "navDate");
            return (Criteria) this;
        }

        public Criteria andNavDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("nav_date not between", value1, value2, "navDate");
            return (Criteria) this;
        }

        public Criteria andDailyGrowthRateIsNull() {
            addCriterion("daily_growth_rate is null");
            return (Criteria) this;
        }

        public Criteria andDailyGrowthRateIsNotNull() {
            addCriterion("daily_growth_rate is not null");
            return (Criteria) this;
        }

        public Criteria andDailyGrowthRateEqualTo(BigDecimal value) {
            addCriterion("daily_growth_rate =", value, "dailyGrowthRate");
            return (Criteria) this;
        }

        public Criteria andDailyGrowthRateNotEqualTo(BigDecimal value) {
            addCriterion("daily_growth_rate <>", value, "dailyGrowthRate");
            return (Criteria) this;
        }

        public Criteria andDailyGrowthRateGreaterThan(BigDecimal value) {
            addCriterion("daily_growth_rate >", value, "dailyGrowthRate");
            return (Criteria) this;
        }

        public Criteria andDailyGrowthRateGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("daily_growth_rate >=", value, "dailyGrowthRate");
            return (Criteria) this;
        }

        public Criteria andDailyGrowthRateLessThan(BigDecimal value) {
            addCriterion("daily_growth_rate <", value, "dailyGrowthRate");
            return (Criteria) this;
        }

        public Criteria andDailyGrowthRateLessThanOrEqualTo(BigDecimal value) {
            addCriterion("daily_growth_rate <=", value, "dailyGrowthRate");
            return (Criteria) this;
        }

        public Criteria andDailyGrowthRateIn(List<BigDecimal> values) {
            addCriterion("daily_growth_rate in", values, "dailyGrowthRate");
            return (Criteria) this;
        }

        public Criteria andDailyGrowthRateNotIn(List<BigDecimal> values) {
            addCriterion("daily_growth_rate not in", values, "dailyGrowthRate");
            return (Criteria) this;
        }

        public Criteria andDailyGrowthRateBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("daily_growth_rate between", value1, value2, "dailyGrowthRate");
            return (Criteria) this;
        }

        public Criteria andDailyGrowthRateNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("daily_growth_rate not between", value1, value2, "dailyGrowthRate");
            return (Criteria) this;
        }

        public Criteria andDividendPerUnitIsNull() {
            addCriterion("dividend_per_unit is null");
            return (Criteria) this;
        }

        public Criteria andDividendPerUnitIsNotNull() {
            addCriterion("dividend_per_unit is not null");
            return (Criteria) this;
        }

        public Criteria andDividendPerUnitEqualTo(BigDecimal value) {
            addCriterion("dividend_per_unit =", value, "dividendPerUnit");
            return (Criteria) this;
        }

        public Criteria andDividendPerUnitNotEqualTo(BigDecimal value) {
            addCriterion("dividend_per_unit <>", value, "dividendPerUnit");
            return (Criteria) this;
        }

        public Criteria andDividendPerUnitGreaterThan(BigDecimal value) {
            addCriterion("dividend_per_unit >", value, "dividendPerUnit");
            return (Criteria) this;
        }

        public Criteria andDividendPerUnitGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("dividend_per_unit >=", value, "dividendPerUnit");
            return (Criteria) this;
        }

        public Criteria andDividendPerUnitLessThan(BigDecimal value) {
            addCriterion("dividend_per_unit <", value, "dividendPerUnit");
            return (Criteria) this;
        }

        public Criteria andDividendPerUnitLessThanOrEqualTo(BigDecimal value) {
            addCriterion("dividend_per_unit <=", value, "dividendPerUnit");
            return (Criteria) this;
        }

        public Criteria andDividendPerUnitIn(List<BigDecimal> values) {
            addCriterion("dividend_per_unit in", values, "dividendPerUnit");
            return (Criteria) this;
        }

        public Criteria andDividendPerUnitNotIn(List<BigDecimal> values) {
            addCriterion("dividend_per_unit not in", values, "dividendPerUnit");
            return (Criteria) this;
        }

        public Criteria andDividendPerUnitBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("dividend_per_unit between", value1, value2, "dividendPerUnit");
            return (Criteria) this;
        }

        public Criteria andDividendPerUnitNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("dividend_per_unit not between", value1, value2, "dividendPerUnit");
            return (Criteria) this;
        }

        public Criteria andAdjustmentFactorIsNull() {
            addCriterion("adjustment_factor is null");
            return (Criteria) this;
        }

        public Criteria andAdjustmentFactorIsNotNull() {
            addCriterion("adjustment_factor is not null");
            return (Criteria) this;
        }

        public Criteria andAdjustmentFactorEqualTo(BigDecimal value) {
            addCriterion("adjustment_factor =", value, "adjustmentFactor");
            return (Criteria) this;
        }

        public Criteria andAdjustmentFactorNotEqualTo(BigDecimal value) {
            addCriterion("adjustment_factor <>", value, "adjustmentFactor");
            return (Criteria) this;
        }

        public Criteria andAdjustmentFactorGreaterThan(BigDecimal value) {
            addCriterion("adjustment_factor >", value, "adjustmentFactor");
            return (Criteria) this;
        }

        public Criteria andAdjustmentFactorGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("adjustment_factor >=", value, "adjustmentFactor");
            return (Criteria) this;
        }

        public Criteria andAdjustmentFactorLessThan(BigDecimal value) {
            addCriterion("adjustment_factor <", value, "adjustmentFactor");
            return (Criteria) this;
        }

        public Criteria andAdjustmentFactorLessThanOrEqualTo(BigDecimal value) {
            addCriterion("adjustment_factor <=", value, "adjustmentFactor");
            return (Criteria) this;
        }

        public Criteria andAdjustmentFactorIn(List<BigDecimal> values) {
            addCriterion("adjustment_factor in", values, "adjustmentFactor");
            return (Criteria) this;
        }

        public Criteria andAdjustmentFactorNotIn(List<BigDecimal> values) {
            addCriterion("adjustment_factor not in", values, "adjustmentFactor");
            return (Criteria) this;
        }

        public Criteria andAdjustmentFactorBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("adjustment_factor between", value1, value2, "adjustmentFactor");
            return (Criteria) this;
        }

        public Criteria andAdjustmentFactorNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("adjustment_factor not between", value1, value2, "adjustmentFactor");
            return (Criteria) this;
        }

        public Criteria andDataSourceIsNull() {
            addCriterion("data_source is null");
            return (Criteria) this;
        }

        public Criteria andDataSourceIsNotNull() {
            addCriterion("data_source is not null");
            return (Criteria) this;
        }

        public Criteria andDataSourceEqualTo(String value) {
            addCriterion("data_source =", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceNotEqualTo(String value) {
            addCriterion("data_source <>", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceGreaterThan(String value) {
            addCriterion("data_source >", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceGreaterThanOrEqualTo(String value) {
            addCriterion("data_source >=", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceLessThan(String value) {
            addCriterion("data_source <", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceLessThanOrEqualTo(String value) {
            addCriterion("data_source <=", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceLike(String value) {
            addCriterion("data_source like", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceNotLike(String value) {
            addCriterion("data_source not like", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceIn(List<String> values) {
            addCriterion("data_source in", values, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceNotIn(List<String> values) {
            addCriterion("data_source not in", values, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceBetween(String value1, String value2) {
            addCriterion("data_source between", value1, value2, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceNotBetween(String value1, String value2) {
            addCriterion("data_source not between", value1, value2, "dataSource");
            return (Criteria) this;
        }

        public Criteria andLastUpdatedIsNull() {
            addCriterion("last_updated is null");
            return (Criteria) this;
        }

        public Criteria andLastUpdatedIsNotNull() {
            addCriterion("last_updated is not null");
            return (Criteria) this;
        }

        public Criteria andLastUpdatedEqualTo(Date value) {
            addCriterion("last_updated =", value, "lastUpdated");
            return (Criteria) this;
        }

        public Criteria andLastUpdatedNotEqualTo(Date value) {
            addCriterion("last_updated <>", value, "lastUpdated");
            return (Criteria) this;
        }

        public Criteria andLastUpdatedGreaterThan(Date value) {
            addCriterion("last_updated >", value, "lastUpdated");
            return (Criteria) this;
        }

        public Criteria andLastUpdatedGreaterThanOrEqualTo(Date value) {
            addCriterion("last_updated >=", value, "lastUpdated");
            return (Criteria) this;
        }

        public Criteria andLastUpdatedLessThan(Date value) {
            addCriterion("last_updated <", value, "lastUpdated");
            return (Criteria) this;
        }

        public Criteria andLastUpdatedLessThanOrEqualTo(Date value) {
            addCriterion("last_updated <=", value, "lastUpdated");
            return (Criteria) this;
        }

        public Criteria andLastUpdatedIn(List<Date> values) {
            addCriterion("last_updated in", values, "lastUpdated");
            return (Criteria) this;
        }

        public Criteria andLastUpdatedNotIn(List<Date> values) {
            addCriterion("last_updated not in", values, "lastUpdated");
            return (Criteria) this;
        }

        public Criteria andLastUpdatedBetween(Date value1, Date value2) {
            addCriterion("last_updated between", value1, value2, "lastUpdated");
            return (Criteria) this;
        }

        public Criteria andLastUpdatedNotBetween(Date value1, Date value2) {
            addCriterion("last_updated not between", value1, value2, "lastUpdated");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}