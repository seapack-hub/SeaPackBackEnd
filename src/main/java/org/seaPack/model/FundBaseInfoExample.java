package org.seaPack.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class FundBaseInfoExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public FundBaseInfoExample() {
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

        public Criteria andFundNameIsNull() {
            addCriterion("fund_name is null");
            return (Criteria) this;
        }

        public Criteria andFundNameIsNotNull() {
            addCriterion("fund_name is not null");
            return (Criteria) this;
        }

        public Criteria andFundNameEqualTo(String value) {
            addCriterion("fund_name =", value, "fundName");
            return (Criteria) this;
        }

        public Criteria andFundNameNotEqualTo(String value) {
            addCriterion("fund_name <>", value, "fundName");
            return (Criteria) this;
        }

        public Criteria andFundNameGreaterThan(String value) {
            addCriterion("fund_name >", value, "fundName");
            return (Criteria) this;
        }

        public Criteria andFundNameGreaterThanOrEqualTo(String value) {
            addCriterion("fund_name >=", value, "fundName");
            return (Criteria) this;
        }

        public Criteria andFundNameLessThan(String value) {
            addCriterion("fund_name <", value, "fundName");
            return (Criteria) this;
        }

        public Criteria andFundNameLessThanOrEqualTo(String value) {
            addCriterion("fund_name <=", value, "fundName");
            return (Criteria) this;
        }

        public Criteria andFundNameLike(String value) {
            addCriterion("fund_name like", value, "fundName");
            return (Criteria) this;
        }

        public Criteria andFundNameNotLike(String value) {
            addCriterion("fund_name not like", value, "fundName");
            return (Criteria) this;
        }

        public Criteria andFundNameIn(List<String> values) {
            addCriterion("fund_name in", values, "fundName");
            return (Criteria) this;
        }

        public Criteria andFundNameNotIn(List<String> values) {
            addCriterion("fund_name not in", values, "fundName");
            return (Criteria) this;
        }

        public Criteria andFundNameBetween(String value1, String value2) {
            addCriterion("fund_name between", value1, value2, "fundName");
            return (Criteria) this;
        }

        public Criteria andFundNameNotBetween(String value1, String value2) {
            addCriterion("fund_name not between", value1, value2, "fundName");
            return (Criteria) this;
        }

        public Criteria andFundFullNameIsNull() {
            addCriterion("fund_full_name is null");
            return (Criteria) this;
        }

        public Criteria andFundFullNameIsNotNull() {
            addCriterion("fund_full_name is not null");
            return (Criteria) this;
        }

        public Criteria andFundFullNameEqualTo(String value) {
            addCriterion("fund_full_name =", value, "fundFullName");
            return (Criteria) this;
        }

        public Criteria andFundFullNameNotEqualTo(String value) {
            addCriterion("fund_full_name <>", value, "fundFullName");
            return (Criteria) this;
        }

        public Criteria andFundFullNameGreaterThan(String value) {
            addCriterion("fund_full_name >", value, "fundFullName");
            return (Criteria) this;
        }

        public Criteria andFundFullNameGreaterThanOrEqualTo(String value) {
            addCriterion("fund_full_name >=", value, "fundFullName");
            return (Criteria) this;
        }

        public Criteria andFundFullNameLessThan(String value) {
            addCriterion("fund_full_name <", value, "fundFullName");
            return (Criteria) this;
        }

        public Criteria andFundFullNameLessThanOrEqualTo(String value) {
            addCriterion("fund_full_name <=", value, "fundFullName");
            return (Criteria) this;
        }

        public Criteria andFundFullNameLike(String value) {
            addCriterion("fund_full_name like", value, "fundFullName");
            return (Criteria) this;
        }

        public Criteria andFundFullNameNotLike(String value) {
            addCriterion("fund_full_name not like", value, "fundFullName");
            return (Criteria) this;
        }

        public Criteria andFundFullNameIn(List<String> values) {
            addCriterion("fund_full_name in", values, "fundFullName");
            return (Criteria) this;
        }

        public Criteria andFundFullNameNotIn(List<String> values) {
            addCriterion("fund_full_name not in", values, "fundFullName");
            return (Criteria) this;
        }

        public Criteria andFundFullNameBetween(String value1, String value2) {
            addCriterion("fund_full_name between", value1, value2, "fundFullName");
            return (Criteria) this;
        }

        public Criteria andFundFullNameNotBetween(String value1, String value2) {
            addCriterion("fund_full_name not between", value1, value2, "fundFullName");
            return (Criteria) this;
        }

        public Criteria andFundTypeIsNull() {
            addCriterion("fund_type is null");
            return (Criteria) this;
        }

        public Criteria andFundTypeIsNotNull() {
            addCriterion("fund_type is not null");
            return (Criteria) this;
        }

        public Criteria andFundTypeEqualTo(String value) {
            addCriterion("fund_type =", value, "fundType");
            return (Criteria) this;
        }

        public Criteria andFundTypeNotEqualTo(String value) {
            addCriterion("fund_type <>", value, "fundType");
            return (Criteria) this;
        }

        public Criteria andFundTypeGreaterThan(String value) {
            addCriterion("fund_type >", value, "fundType");
            return (Criteria) this;
        }

        public Criteria andFundTypeGreaterThanOrEqualTo(String value) {
            addCriterion("fund_type >=", value, "fundType");
            return (Criteria) this;
        }

        public Criteria andFundTypeLessThan(String value) {
            addCriterion("fund_type <", value, "fundType");
            return (Criteria) this;
        }

        public Criteria andFundTypeLessThanOrEqualTo(String value) {
            addCriterion("fund_type <=", value, "fundType");
            return (Criteria) this;
        }

        public Criteria andFundTypeLike(String value) {
            addCriterion("fund_type like", value, "fundType");
            return (Criteria) this;
        }

        public Criteria andFundTypeNotLike(String value) {
            addCriterion("fund_type not like", value, "fundType");
            return (Criteria) this;
        }

        public Criteria andFundTypeIn(List<String> values) {
            addCriterion("fund_type in", values, "fundType");
            return (Criteria) this;
        }

        public Criteria andFundTypeNotIn(List<String> values) {
            addCriterion("fund_type not in", values, "fundType");
            return (Criteria) this;
        }

        public Criteria andFundTypeBetween(String value1, String value2) {
            addCriterion("fund_type between", value1, value2, "fundType");
            return (Criteria) this;
        }

        public Criteria andFundTypeNotBetween(String value1, String value2) {
            addCriterion("fund_type not between", value1, value2, "fundType");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyIsNull() {
            addCriterion("management_company is null");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyIsNotNull() {
            addCriterion("management_company is not null");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyEqualTo(String value) {
            addCriterion("management_company =", value, "managementCompany");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyNotEqualTo(String value) {
            addCriterion("management_company <>", value, "managementCompany");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyGreaterThan(String value) {
            addCriterion("management_company >", value, "managementCompany");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyGreaterThanOrEqualTo(String value) {
            addCriterion("management_company >=", value, "managementCompany");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyLessThan(String value) {
            addCriterion("management_company <", value, "managementCompany");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyLessThanOrEqualTo(String value) {
            addCriterion("management_company <=", value, "managementCompany");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyLike(String value) {
            addCriterion("management_company like", value, "managementCompany");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyNotLike(String value) {
            addCriterion("management_company not like", value, "managementCompany");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyIn(List<String> values) {
            addCriterion("management_company in", values, "managementCompany");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyNotIn(List<String> values) {
            addCriterion("management_company not in", values, "managementCompany");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyBetween(String value1, String value2) {
            addCriterion("management_company between", value1, value2, "managementCompany");
            return (Criteria) this;
        }

        public Criteria andManagementCompanyNotBetween(String value1, String value2) {
            addCriterion("management_company not between", value1, value2, "managementCompany");
            return (Criteria) this;
        }

        public Criteria andCustodianIsNull() {
            addCriterion("custodian is null");
            return (Criteria) this;
        }

        public Criteria andCustodianIsNotNull() {
            addCriterion("custodian is not null");
            return (Criteria) this;
        }

        public Criteria andCustodianEqualTo(String value) {
            addCriterion("custodian =", value, "custodian");
            return (Criteria) this;
        }

        public Criteria andCustodianNotEqualTo(String value) {
            addCriterion("custodian <>", value, "custodian");
            return (Criteria) this;
        }

        public Criteria andCustodianGreaterThan(String value) {
            addCriterion("custodian >", value, "custodian");
            return (Criteria) this;
        }

        public Criteria andCustodianGreaterThanOrEqualTo(String value) {
            addCriterion("custodian >=", value, "custodian");
            return (Criteria) this;
        }

        public Criteria andCustodianLessThan(String value) {
            addCriterion("custodian <", value, "custodian");
            return (Criteria) this;
        }

        public Criteria andCustodianLessThanOrEqualTo(String value) {
            addCriterion("custodian <=", value, "custodian");
            return (Criteria) this;
        }

        public Criteria andCustodianLike(String value) {
            addCriterion("custodian like", value, "custodian");
            return (Criteria) this;
        }

        public Criteria andCustodianNotLike(String value) {
            addCriterion("custodian not like", value, "custodian");
            return (Criteria) this;
        }

        public Criteria andCustodianIn(List<String> values) {
            addCriterion("custodian in", values, "custodian");
            return (Criteria) this;
        }

        public Criteria andCustodianNotIn(List<String> values) {
            addCriterion("custodian not in", values, "custodian");
            return (Criteria) this;
        }

        public Criteria andCustodianBetween(String value1, String value2) {
            addCriterion("custodian between", value1, value2, "custodian");
            return (Criteria) this;
        }

        public Criteria andCustodianNotBetween(String value1, String value2) {
            addCriterion("custodian not between", value1, value2, "custodian");
            return (Criteria) this;
        }

        public Criteria andInceptDateIsNull() {
            addCriterion("incept_date is null");
            return (Criteria) this;
        }

        public Criteria andInceptDateIsNotNull() {
            addCriterion("incept_date is not null");
            return (Criteria) this;
        }

        public Criteria andInceptDateEqualTo(Date value) {
            addCriterionForJDBCDate("incept_date =", value, "inceptDate");
            return (Criteria) this;
        }

        public Criteria andInceptDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("incept_date <>", value, "inceptDate");
            return (Criteria) this;
        }

        public Criteria andInceptDateGreaterThan(Date value) {
            addCriterionForJDBCDate("incept_date >", value, "inceptDate");
            return (Criteria) this;
        }

        public Criteria andInceptDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("incept_date >=", value, "inceptDate");
            return (Criteria) this;
        }

        public Criteria andInceptDateLessThan(Date value) {
            addCriterionForJDBCDate("incept_date <", value, "inceptDate");
            return (Criteria) this;
        }

        public Criteria andInceptDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("incept_date <=", value, "inceptDate");
            return (Criteria) this;
        }

        public Criteria andInceptDateIn(List<Date> values) {
            addCriterionForJDBCDate("incept_date in", values, "inceptDate");
            return (Criteria) this;
        }

        public Criteria andInceptDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("incept_date not in", values, "inceptDate");
            return (Criteria) this;
        }

        public Criteria andInceptDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("incept_date between", value1, value2, "inceptDate");
            return (Criteria) this;
        }

        public Criteria andInceptDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("incept_date not between", value1, value2, "inceptDate");
            return (Criteria) this;
        }

        public Criteria andIssueShareIsNull() {
            addCriterion("issue_share is null");
            return (Criteria) this;
        }

        public Criteria andIssueShareIsNotNull() {
            addCriterion("issue_share is not null");
            return (Criteria) this;
        }

        public Criteria andIssueShareEqualTo(BigDecimal value) {
            addCriterion("issue_share =", value, "issueShare");
            return (Criteria) this;
        }

        public Criteria andIssueShareNotEqualTo(BigDecimal value) {
            addCriterion("issue_share <>", value, "issueShare");
            return (Criteria) this;
        }

        public Criteria andIssueShareGreaterThan(BigDecimal value) {
            addCriterion("issue_share >", value, "issueShare");
            return (Criteria) this;
        }

        public Criteria andIssueShareGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("issue_share >=", value, "issueShare");
            return (Criteria) this;
        }

        public Criteria andIssueShareLessThan(BigDecimal value) {
            addCriterion("issue_share <", value, "issueShare");
            return (Criteria) this;
        }

        public Criteria andIssueShareLessThanOrEqualTo(BigDecimal value) {
            addCriterion("issue_share <=", value, "issueShare");
            return (Criteria) this;
        }

        public Criteria andIssueShareIn(List<BigDecimal> values) {
            addCriterion("issue_share in", values, "issueShare");
            return (Criteria) this;
        }

        public Criteria andIssueShareNotIn(List<BigDecimal> values) {
            addCriterion("issue_share not in", values, "issueShare");
            return (Criteria) this;
        }

        public Criteria andIssueShareBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("issue_share between", value1, value2, "issueShare");
            return (Criteria) this;
        }

        public Criteria andIssueShareNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("issue_share not between", value1, value2, "issueShare");
            return (Criteria) this;
        }

        public Criteria andMFeeIsNull() {
            addCriterion("m_fee is null");
            return (Criteria) this;
        }

        public Criteria andMFeeIsNotNull() {
            addCriterion("m_fee is not null");
            return (Criteria) this;
        }

        public Criteria andMFeeEqualTo(BigDecimal value) {
            addCriterion("m_fee =", value, "mFee");
            return (Criteria) this;
        }

        public Criteria andMFeeNotEqualTo(BigDecimal value) {
            addCriterion("m_fee <>", value, "mFee");
            return (Criteria) this;
        }

        public Criteria andMFeeGreaterThan(BigDecimal value) {
            addCriterion("m_fee >", value, "mFee");
            return (Criteria) this;
        }

        public Criteria andMFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("m_fee >=", value, "mFee");
            return (Criteria) this;
        }

        public Criteria andMFeeLessThan(BigDecimal value) {
            addCriterion("m_fee <", value, "mFee");
            return (Criteria) this;
        }

        public Criteria andMFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("m_fee <=", value, "mFee");
            return (Criteria) this;
        }

        public Criteria andMFeeIn(List<BigDecimal> values) {
            addCriterion("m_fee in", values, "mFee");
            return (Criteria) this;
        }

        public Criteria andMFeeNotIn(List<BigDecimal> values) {
            addCriterion("m_fee not in", values, "mFee");
            return (Criteria) this;
        }

        public Criteria andMFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("m_fee between", value1, value2, "mFee");
            return (Criteria) this;
        }

        public Criteria andMFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("m_fee not between", value1, value2, "mFee");
            return (Criteria) this;
        }

        public Criteria andCFeeIsNull() {
            addCriterion("c_fee is null");
            return (Criteria) this;
        }

        public Criteria andCFeeIsNotNull() {
            addCriterion("c_fee is not null");
            return (Criteria) this;
        }

        public Criteria andCFeeEqualTo(BigDecimal value) {
            addCriterion("c_fee =", value, "cFee");
            return (Criteria) this;
        }

        public Criteria andCFeeNotEqualTo(BigDecimal value) {
            addCriterion("c_fee <>", value, "cFee");
            return (Criteria) this;
        }

        public Criteria andCFeeGreaterThan(BigDecimal value) {
            addCriterion("c_fee >", value, "cFee");
            return (Criteria) this;
        }

        public Criteria andCFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("c_fee >=", value, "cFee");
            return (Criteria) this;
        }

        public Criteria andCFeeLessThan(BigDecimal value) {
            addCriterion("c_fee <", value, "cFee");
            return (Criteria) this;
        }

        public Criteria andCFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("c_fee <=", value, "cFee");
            return (Criteria) this;
        }

        public Criteria andCFeeIn(List<BigDecimal> values) {
            addCriterion("c_fee in", values, "cFee");
            return (Criteria) this;
        }

        public Criteria andCFeeNotIn(List<BigDecimal> values) {
            addCriterion("c_fee not in", values, "cFee");
            return (Criteria) this;
        }

        public Criteria andCFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("c_fee between", value1, value2, "cFee");
            return (Criteria) this;
        }

        public Criteria andCFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("c_fee not between", value1, value2, "cFee");
            return (Criteria) this;
        }

        public Criteria andSFeeIsNull() {
            addCriterion("s_fee is null");
            return (Criteria) this;
        }

        public Criteria andSFeeIsNotNull() {
            addCriterion("s_fee is not null");
            return (Criteria) this;
        }

        public Criteria andSFeeEqualTo(BigDecimal value) {
            addCriterion("s_fee =", value, "sFee");
            return (Criteria) this;
        }

        public Criteria andSFeeNotEqualTo(BigDecimal value) {
            addCriterion("s_fee <>", value, "sFee");
            return (Criteria) this;
        }

        public Criteria andSFeeGreaterThan(BigDecimal value) {
            addCriterion("s_fee >", value, "sFee");
            return (Criteria) this;
        }

        public Criteria andSFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("s_fee >=", value, "sFee");
            return (Criteria) this;
        }

        public Criteria andSFeeLessThan(BigDecimal value) {
            addCriterion("s_fee <", value, "sFee");
            return (Criteria) this;
        }

        public Criteria andSFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("s_fee <=", value, "sFee");
            return (Criteria) this;
        }

        public Criteria andSFeeIn(List<BigDecimal> values) {
            addCriterion("s_fee in", values, "sFee");
            return (Criteria) this;
        }

        public Criteria andSFeeNotIn(List<BigDecimal> values) {
            addCriterion("s_fee not in", values, "sFee");
            return (Criteria) this;
        }

        public Criteria andSFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("s_fee between", value1, value2, "sFee");
            return (Criteria) this;
        }

        public Criteria andSFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("s_fee not between", value1, value2, "sFee");
            return (Criteria) this;
        }

        public Criteria andLatestAssetSizeIsNull() {
            addCriterion("latest_asset_size is null");
            return (Criteria) this;
        }

        public Criteria andLatestAssetSizeIsNotNull() {
            addCriterion("latest_asset_size is not null");
            return (Criteria) this;
        }

        public Criteria andLatestAssetSizeEqualTo(BigDecimal value) {
            addCriterion("latest_asset_size =", value, "latestAssetSize");
            return (Criteria) this;
        }

        public Criteria andLatestAssetSizeNotEqualTo(BigDecimal value) {
            addCriterion("latest_asset_size <>", value, "latestAssetSize");
            return (Criteria) this;
        }

        public Criteria andLatestAssetSizeGreaterThan(BigDecimal value) {
            addCriterion("latest_asset_size >", value, "latestAssetSize");
            return (Criteria) this;
        }

        public Criteria andLatestAssetSizeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("latest_asset_size >=", value, "latestAssetSize");
            return (Criteria) this;
        }

        public Criteria andLatestAssetSizeLessThan(BigDecimal value) {
            addCriterion("latest_asset_size <", value, "latestAssetSize");
            return (Criteria) this;
        }

        public Criteria andLatestAssetSizeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("latest_asset_size <=", value, "latestAssetSize");
            return (Criteria) this;
        }

        public Criteria andLatestAssetSizeIn(List<BigDecimal> values) {
            addCriterion("latest_asset_size in", values, "latestAssetSize");
            return (Criteria) this;
        }

        public Criteria andLatestAssetSizeNotIn(List<BigDecimal> values) {
            addCriterion("latest_asset_size not in", values, "latestAssetSize");
            return (Criteria) this;
        }

        public Criteria andLatestAssetSizeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("latest_asset_size between", value1, value2, "latestAssetSize");
            return (Criteria) this;
        }

        public Criteria andLatestAssetSizeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("latest_asset_size not between", value1, value2, "latestAssetSize");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("status like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("status not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIsNull() {
            addCriterion("created_at is null");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIsNotNull() {
            addCriterion("created_at is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedAtEqualTo(Date value) {
            addCriterion("created_at =", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotEqualTo(Date value) {
            addCriterion("created_at <>", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtGreaterThan(Date value) {
            addCriterion("created_at >", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtGreaterThanOrEqualTo(Date value) {
            addCriterion("created_at >=", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtLessThan(Date value) {
            addCriterion("created_at <", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtLessThanOrEqualTo(Date value) {
            addCriterion("created_at <=", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIn(List<Date> values) {
            addCriterion("created_at in", values, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotIn(List<Date> values) {
            addCriterion("created_at not in", values, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtBetween(Date value1, Date value2) {
            addCriterion("created_at between", value1, value2, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotBetween(Date value1, Date value2) {
            addCriterion("created_at not between", value1, value2, "createdAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIsNull() {
            addCriterion("updated_at is null");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIsNotNull() {
            addCriterion("updated_at is not null");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtEqualTo(Date value) {
            addCriterion("updated_at =", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotEqualTo(Date value) {
            addCriterion("updated_at <>", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtGreaterThan(Date value) {
            addCriterion("updated_at >", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtGreaterThanOrEqualTo(Date value) {
            addCriterion("updated_at >=", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtLessThan(Date value) {
            addCriterion("updated_at <", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtLessThanOrEqualTo(Date value) {
            addCriterion("updated_at <=", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIn(List<Date> values) {
            addCriterion("updated_at in", values, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotIn(List<Date> values) {
            addCriterion("updated_at not in", values, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtBetween(Date value1, Date value2) {
            addCriterion("updated_at between", value1, value2, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotBetween(Date value1, Date value2) {
            addCriterion("updated_at not between", value1, value2, "updatedAt");
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