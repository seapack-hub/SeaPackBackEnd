package org.seaPack.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HoldingExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public HoldingExample() {
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

        public Criteria andUserIdIsNull() {
            addCriterion("user_id is null");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNotNull() {
            addCriterion("user_id is not null");
            return (Criteria) this;
        }

        public Criteria andUserIdEqualTo(Integer value) {
            addCriterion("user_id =", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotEqualTo(Integer value) {
            addCriterion("user_id <>", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThan(Integer value) {
            addCriterion("user_id >", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("user_id >=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThan(Integer value) {
            addCriterion("user_id <", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThanOrEqualTo(Integer value) {
            addCriterion("user_id <=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdIn(List<Integer> values) {
            addCriterion("user_id in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotIn(List<Integer> values) {
            addCriterion("user_id not in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdBetween(Integer value1, Integer value2) {
            addCriterion("user_id between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotBetween(Integer value1, Integer value2) {
            addCriterion("user_id not between", value1, value2, "userId");
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

        public Criteria andTotalSharesIsNull() {
            addCriterion("total_shares is null");
            return (Criteria) this;
        }

        public Criteria andTotalSharesIsNotNull() {
            addCriterion("total_shares is not null");
            return (Criteria) this;
        }

        public Criteria andTotalSharesEqualTo(BigDecimal value) {
            addCriterion("total_shares =", value, "totalShares");
            return (Criteria) this;
        }

        public Criteria andTotalSharesNotEqualTo(BigDecimal value) {
            addCriterion("total_shares <>", value, "totalShares");
            return (Criteria) this;
        }

        public Criteria andTotalSharesGreaterThan(BigDecimal value) {
            addCriterion("total_shares >", value, "totalShares");
            return (Criteria) this;
        }

        public Criteria andTotalSharesGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("total_shares >=", value, "totalShares");
            return (Criteria) this;
        }

        public Criteria andTotalSharesLessThan(BigDecimal value) {
            addCriterion("total_shares <", value, "totalShares");
            return (Criteria) this;
        }

        public Criteria andTotalSharesLessThanOrEqualTo(BigDecimal value) {
            addCriterion("total_shares <=", value, "totalShares");
            return (Criteria) this;
        }

        public Criteria andTotalSharesIn(List<BigDecimal> values) {
            addCriterion("total_shares in", values, "totalShares");
            return (Criteria) this;
        }

        public Criteria andTotalSharesNotIn(List<BigDecimal> values) {
            addCriterion("total_shares not in", values, "totalShares");
            return (Criteria) this;
        }

        public Criteria andTotalSharesBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("total_shares between", value1, value2, "totalShares");
            return (Criteria) this;
        }

        public Criteria andTotalSharesNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("total_shares not between", value1, value2, "totalShares");
            return (Criteria) this;
        }

        public Criteria andAvailableSharesIsNull() {
            addCriterion("available_shares is null");
            return (Criteria) this;
        }

        public Criteria andAvailableSharesIsNotNull() {
            addCriterion("available_shares is not null");
            return (Criteria) this;
        }

        public Criteria andAvailableSharesEqualTo(BigDecimal value) {
            addCriterion("available_shares =", value, "availableShares");
            return (Criteria) this;
        }

        public Criteria andAvailableSharesNotEqualTo(BigDecimal value) {
            addCriterion("available_shares <>", value, "availableShares");
            return (Criteria) this;
        }

        public Criteria andAvailableSharesGreaterThan(BigDecimal value) {
            addCriterion("available_shares >", value, "availableShares");
            return (Criteria) this;
        }

        public Criteria andAvailableSharesGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("available_shares >=", value, "availableShares");
            return (Criteria) this;
        }

        public Criteria andAvailableSharesLessThan(BigDecimal value) {
            addCriterion("available_shares <", value, "availableShares");
            return (Criteria) this;
        }

        public Criteria andAvailableSharesLessThanOrEqualTo(BigDecimal value) {
            addCriterion("available_shares <=", value, "availableShares");
            return (Criteria) this;
        }

        public Criteria andAvailableSharesIn(List<BigDecimal> values) {
            addCriterion("available_shares in", values, "availableShares");
            return (Criteria) this;
        }

        public Criteria andAvailableSharesNotIn(List<BigDecimal> values) {
            addCriterion("available_shares not in", values, "availableShares");
            return (Criteria) this;
        }

        public Criteria andAvailableSharesBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("available_shares between", value1, value2, "availableShares");
            return (Criteria) this;
        }

        public Criteria andAvailableSharesNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("available_shares not between", value1, value2, "availableShares");
            return (Criteria) this;
        }

        public Criteria andFrozenSharesIsNull() {
            addCriterion("frozen_shares is null");
            return (Criteria) this;
        }

        public Criteria andFrozenSharesIsNotNull() {
            addCriterion("frozen_shares is not null");
            return (Criteria) this;
        }

        public Criteria andFrozenSharesEqualTo(BigDecimal value) {
            addCriterion("frozen_shares =", value, "frozenShares");
            return (Criteria) this;
        }

        public Criteria andFrozenSharesNotEqualTo(BigDecimal value) {
            addCriterion("frozen_shares <>", value, "frozenShares");
            return (Criteria) this;
        }

        public Criteria andFrozenSharesGreaterThan(BigDecimal value) {
            addCriterion("frozen_shares >", value, "frozenShares");
            return (Criteria) this;
        }

        public Criteria andFrozenSharesGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("frozen_shares >=", value, "frozenShares");
            return (Criteria) this;
        }

        public Criteria andFrozenSharesLessThan(BigDecimal value) {
            addCriterion("frozen_shares <", value, "frozenShares");
            return (Criteria) this;
        }

        public Criteria andFrozenSharesLessThanOrEqualTo(BigDecimal value) {
            addCriterion("frozen_shares <=", value, "frozenShares");
            return (Criteria) this;
        }

        public Criteria andFrozenSharesIn(List<BigDecimal> values) {
            addCriterion("frozen_shares in", values, "frozenShares");
            return (Criteria) this;
        }

        public Criteria andFrozenSharesNotIn(List<BigDecimal> values) {
            addCriterion("frozen_shares not in", values, "frozenShares");
            return (Criteria) this;
        }

        public Criteria andFrozenSharesBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("frozen_shares between", value1, value2, "frozenShares");
            return (Criteria) this;
        }

        public Criteria andFrozenSharesNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("frozen_shares not between", value1, value2, "frozenShares");
            return (Criteria) this;
        }

        public Criteria andAvgCostPriceIsNull() {
            addCriterion("avg_cost_price is null");
            return (Criteria) this;
        }

        public Criteria andAvgCostPriceIsNotNull() {
            addCriterion("avg_cost_price is not null");
            return (Criteria) this;
        }

        public Criteria andAvgCostPriceEqualTo(BigDecimal value) {
            addCriterion("avg_cost_price =", value, "avgCostPrice");
            return (Criteria) this;
        }

        public Criteria andAvgCostPriceNotEqualTo(BigDecimal value) {
            addCriterion("avg_cost_price <>", value, "avgCostPrice");
            return (Criteria) this;
        }

        public Criteria andAvgCostPriceGreaterThan(BigDecimal value) {
            addCriterion("avg_cost_price >", value, "avgCostPrice");
            return (Criteria) this;
        }

        public Criteria andAvgCostPriceGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("avg_cost_price >=", value, "avgCostPrice");
            return (Criteria) this;
        }

        public Criteria andAvgCostPriceLessThan(BigDecimal value) {
            addCriterion("avg_cost_price <", value, "avgCostPrice");
            return (Criteria) this;
        }

        public Criteria andAvgCostPriceLessThanOrEqualTo(BigDecimal value) {
            addCriterion("avg_cost_price <=", value, "avgCostPrice");
            return (Criteria) this;
        }

        public Criteria andAvgCostPriceIn(List<BigDecimal> values) {
            addCriterion("avg_cost_price in", values, "avgCostPrice");
            return (Criteria) this;
        }

        public Criteria andAvgCostPriceNotIn(List<BigDecimal> values) {
            addCriterion("avg_cost_price not in", values, "avgCostPrice");
            return (Criteria) this;
        }

        public Criteria andAvgCostPriceBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("avg_cost_price between", value1, value2, "avgCostPrice");
            return (Criteria) this;
        }

        public Criteria andAvgCostPriceNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("avg_cost_price not between", value1, value2, "avgCostPrice");
            return (Criteria) this;
        }

        public Criteria andTotalCostIsNull() {
            addCriterion("total_cost is null");
            return (Criteria) this;
        }

        public Criteria andTotalCostIsNotNull() {
            addCriterion("total_cost is not null");
            return (Criteria) this;
        }

        public Criteria andTotalCostEqualTo(BigDecimal value) {
            addCriterion("total_cost =", value, "totalCost");
            return (Criteria) this;
        }

        public Criteria andTotalCostNotEqualTo(BigDecimal value) {
            addCriterion("total_cost <>", value, "totalCost");
            return (Criteria) this;
        }

        public Criteria andTotalCostGreaterThan(BigDecimal value) {
            addCriterion("total_cost >", value, "totalCost");
            return (Criteria) this;
        }

        public Criteria andTotalCostGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("total_cost >=", value, "totalCost");
            return (Criteria) this;
        }

        public Criteria andTotalCostLessThan(BigDecimal value) {
            addCriterion("total_cost <", value, "totalCost");
            return (Criteria) this;
        }

        public Criteria andTotalCostLessThanOrEqualTo(BigDecimal value) {
            addCriterion("total_cost <=", value, "totalCost");
            return (Criteria) this;
        }

        public Criteria andTotalCostIn(List<BigDecimal> values) {
            addCriterion("total_cost in", values, "totalCost");
            return (Criteria) this;
        }

        public Criteria andTotalCostNotIn(List<BigDecimal> values) {
            addCriterion("total_cost not in", values, "totalCost");
            return (Criteria) this;
        }

        public Criteria andTotalCostBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("total_cost between", value1, value2, "totalCost");
            return (Criteria) this;
        }

        public Criteria andTotalCostNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("total_cost not between", value1, value2, "totalCost");
            return (Criteria) this;
        }

        public Criteria andCostPrincipalIsNull() {
            addCriterion("cost_principal is null");
            return (Criteria) this;
        }

        public Criteria andCostPrincipalIsNotNull() {
            addCriterion("cost_principal is not null");
            return (Criteria) this;
        }

        public Criteria andCostPrincipalEqualTo(BigDecimal value) {
            addCriterion("cost_principal =", value, "costPrincipal");
            return (Criteria) this;
        }

        public Criteria andCostPrincipalNotEqualTo(BigDecimal value) {
            addCriterion("cost_principal <>", value, "costPrincipal");
            return (Criteria) this;
        }

        public Criteria andCostPrincipalGreaterThan(BigDecimal value) {
            addCriterion("cost_principal >", value, "costPrincipal");
            return (Criteria) this;
        }

        public Criteria andCostPrincipalGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("cost_principal >=", value, "costPrincipal");
            return (Criteria) this;
        }

        public Criteria andCostPrincipalLessThan(BigDecimal value) {
            addCriterion("cost_principal <", value, "costPrincipal");
            return (Criteria) this;
        }

        public Criteria andCostPrincipalLessThanOrEqualTo(BigDecimal value) {
            addCriterion("cost_principal <=", value, "costPrincipal");
            return (Criteria) this;
        }

        public Criteria andCostPrincipalIn(List<BigDecimal> values) {
            addCriterion("cost_principal in", values, "costPrincipal");
            return (Criteria) this;
        }

        public Criteria andCostPrincipalNotIn(List<BigDecimal> values) {
            addCriterion("cost_principal not in", values, "costPrincipal");
            return (Criteria) this;
        }

        public Criteria andCostPrincipalBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("cost_principal between", value1, value2, "costPrincipal");
            return (Criteria) this;
        }

        public Criteria andCostPrincipalNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("cost_principal not between", value1, value2, "costPrincipal");
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