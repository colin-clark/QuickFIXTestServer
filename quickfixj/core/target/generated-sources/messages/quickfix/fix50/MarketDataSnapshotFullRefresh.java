package quickfix.fix50;

import quickfix.FieldNotFound;
import quickfix.Group;


public class MarketDataSnapshotFullRefresh extends Message {
    static final long serialVersionUID = 20050617;
    public static final String MSGTYPE = "W";

    public MarketDataSnapshotFullRefresh() {
        super();
        getHeader().setField(new quickfix.field.MsgType(MSGTYPE));
    }

    public void set(quickfix.field.MDReqID value) {
        setField(value);
    }

    public quickfix.field.MDReqID get(quickfix.field.MDReqID value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.MDReqID getMDReqID() throws FieldNotFound {
        quickfix.field.MDReqID value = new quickfix.field.MDReqID();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.MDReqID field) {
        return isSetField(field);
    }

    public boolean isSetMDReqID() {
        return isSetField(262);
    }

    public void set(quickfix.fix50.component.Instrument component) {
        setComponent(component);
    }

    public quickfix.fix50.component.Instrument get(
        quickfix.fix50.component.Instrument component)
        throws FieldNotFound {
        getComponent(component);

        return component;
    }

    public quickfix.fix50.component.Instrument getInstrument()
        throws FieldNotFound {
        quickfix.fix50.component.Instrument component = new quickfix.fix50.component.Instrument();
        getComponent(component);

        return component;
    }

    public void set(quickfix.field.Symbol value) {
        setField(value);
    }

    public quickfix.field.Symbol get(quickfix.field.Symbol value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.Symbol getSymbol() throws FieldNotFound {
        quickfix.field.Symbol value = new quickfix.field.Symbol();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.Symbol field) {
        return isSetField(field);
    }

    public boolean isSetSymbol() {
        return isSetField(55);
    }

    public void set(quickfix.field.SymbolSfx value) {
        setField(value);
    }

    public quickfix.field.SymbolSfx get(quickfix.field.SymbolSfx value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.SymbolSfx getSymbolSfx() throws FieldNotFound {
        quickfix.field.SymbolSfx value = new quickfix.field.SymbolSfx();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.SymbolSfx field) {
        return isSetField(field);
    }

    public boolean isSetSymbolSfx() {
        return isSetField(65);
    }

    public void set(quickfix.field.SecurityID value) {
        setField(value);
    }

    public quickfix.field.SecurityID get(quickfix.field.SecurityID value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.SecurityID getSecurityID() throws FieldNotFound {
        quickfix.field.SecurityID value = new quickfix.field.SecurityID();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.SecurityID field) {
        return isSetField(field);
    }

    public boolean isSetSecurityID() {
        return isSetField(48);
    }

    public void set(quickfix.field.SecurityIDSource value) {
        setField(value);
    }

    public quickfix.field.SecurityIDSource get(
        quickfix.field.SecurityIDSource value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.SecurityIDSource getSecurityIDSource()
        throws FieldNotFound {
        quickfix.field.SecurityIDSource value = new quickfix.field.SecurityIDSource();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.SecurityIDSource field) {
        return isSetField(field);
    }

    public boolean isSetSecurityIDSource() {
        return isSetField(22);
    }

    public void set(quickfix.fix50.component.SecAltIDGrp component) {
        setComponent(component);
    }

    public quickfix.fix50.component.SecAltIDGrp get(
        quickfix.fix50.component.SecAltIDGrp component)
        throws FieldNotFound {
        getComponent(component);

        return component;
    }

    public quickfix.fix50.component.SecAltIDGrp getSecAltIDGrp()
        throws FieldNotFound {
        quickfix.fix50.component.SecAltIDGrp component = new quickfix.fix50.component.SecAltIDGrp();
        getComponent(component);

        return component;
    }

    public void set(quickfix.field.NoSecurityAltID value) {
        setField(value);
    }

    public quickfix.field.NoSecurityAltID get(
        quickfix.field.NoSecurityAltID value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.NoSecurityAltID getNoSecurityAltID()
        throws FieldNotFound {
        quickfix.field.NoSecurityAltID value = new quickfix.field.NoSecurityAltID();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.NoSecurityAltID field) {
        return isSetField(field);
    }

    public boolean isSetNoSecurityAltID() {
        return isSetField(454);
    }

    public void set(quickfix.field.Product value) {
        setField(value);
    }

    public quickfix.field.Product get(quickfix.field.Product value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.Product getProduct() throws FieldNotFound {
        quickfix.field.Product value = new quickfix.field.Product();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.Product field) {
        return isSetField(field);
    }

    public boolean isSetProduct() {
        return isSetField(460);
    }

    public void set(quickfix.field.CFICode value) {
        setField(value);
    }

    public quickfix.field.CFICode get(quickfix.field.CFICode value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.CFICode getCFICode() throws FieldNotFound {
        quickfix.field.CFICode value = new quickfix.field.CFICode();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.CFICode field) {
        return isSetField(field);
    }

    public boolean isSetCFICode() {
        return isSetField(461);
    }

    public void set(quickfix.field.SecurityType value) {
        setField(value);
    }

    public quickfix.field.SecurityType get(quickfix.field.SecurityType value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.SecurityType getSecurityType()
        throws FieldNotFound {
        quickfix.field.SecurityType value = new quickfix.field.SecurityType();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.SecurityType field) {
        return isSetField(field);
    }

    public boolean isSetSecurityType() {
        return isSetField(167);
    }

    public void set(quickfix.field.SecuritySubType value) {
        setField(value);
    }

    public quickfix.field.SecuritySubType get(
        quickfix.field.SecuritySubType value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.SecuritySubType getSecuritySubType()
        throws FieldNotFound {
        quickfix.field.SecuritySubType value = new quickfix.field.SecuritySubType();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.SecuritySubType field) {
        return isSetField(field);
    }

    public boolean isSetSecuritySubType() {
        return isSetField(762);
    }

    public void set(quickfix.field.MaturityMonthYear value) {
        setField(value);
    }

    public quickfix.field.MaturityMonthYear get(
        quickfix.field.MaturityMonthYear value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.MaturityMonthYear getMaturityMonthYear()
        throws FieldNotFound {
        quickfix.field.MaturityMonthYear value = new quickfix.field.MaturityMonthYear();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.MaturityMonthYear field) {
        return isSetField(field);
    }

    public boolean isSetMaturityMonthYear() {
        return isSetField(200);
    }

    public void set(quickfix.field.MaturityDate value) {
        setField(value);
    }

    public quickfix.field.MaturityDate get(quickfix.field.MaturityDate value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.MaturityDate getMaturityDate()
        throws FieldNotFound {
        quickfix.field.MaturityDate value = new quickfix.field.MaturityDate();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.MaturityDate field) {
        return isSetField(field);
    }

    public boolean isSetMaturityDate() {
        return isSetField(541);
    }

    public void set(quickfix.field.CouponPaymentDate value) {
        setField(value);
    }

    public quickfix.field.CouponPaymentDate get(
        quickfix.field.CouponPaymentDate value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.CouponPaymentDate getCouponPaymentDate()
        throws FieldNotFound {
        quickfix.field.CouponPaymentDate value = new quickfix.field.CouponPaymentDate();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.CouponPaymentDate field) {
        return isSetField(field);
    }

    public boolean isSetCouponPaymentDate() {
        return isSetField(224);
    }

    public void set(quickfix.field.IssueDate value) {
        setField(value);
    }

    public quickfix.field.IssueDate get(quickfix.field.IssueDate value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.IssueDate getIssueDate() throws FieldNotFound {
        quickfix.field.IssueDate value = new quickfix.field.IssueDate();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.IssueDate field) {
        return isSetField(field);
    }

    public boolean isSetIssueDate() {
        return isSetField(225);
    }

    public void set(quickfix.field.RepoCollateralSecurityType value) {
        setField(value);
    }

    public quickfix.field.RepoCollateralSecurityType get(
        quickfix.field.RepoCollateralSecurityType value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.RepoCollateralSecurityType getRepoCollateralSecurityType()
        throws FieldNotFound {
        quickfix.field.RepoCollateralSecurityType value = new quickfix.field.RepoCollateralSecurityType();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.RepoCollateralSecurityType field) {
        return isSetField(field);
    }

    public boolean isSetRepoCollateralSecurityType() {
        return isSetField(239);
    }

    public void set(quickfix.field.RepurchaseTerm value) {
        setField(value);
    }

    public quickfix.field.RepurchaseTerm get(
        quickfix.field.RepurchaseTerm value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.RepurchaseTerm getRepurchaseTerm()
        throws FieldNotFound {
        quickfix.field.RepurchaseTerm value = new quickfix.field.RepurchaseTerm();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.RepurchaseTerm field) {
        return isSetField(field);
    }

    public boolean isSetRepurchaseTerm() {
        return isSetField(226);
    }

    public void set(quickfix.field.RepurchaseRate value) {
        setField(value);
    }

    public quickfix.field.RepurchaseRate get(
        quickfix.field.RepurchaseRate value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.RepurchaseRate getRepurchaseRate()
        throws FieldNotFound {
        quickfix.field.RepurchaseRate value = new quickfix.field.RepurchaseRate();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.RepurchaseRate field) {
        return isSetField(field);
    }

    public boolean isSetRepurchaseRate() {
        return isSetField(227);
    }

    public void set(quickfix.field.Factor value) {
        setField(value);
    }

    public quickfix.field.Factor get(quickfix.field.Factor value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.Factor getFactor() throws FieldNotFound {
        quickfix.field.Factor value = new quickfix.field.Factor();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.Factor field) {
        return isSetField(field);
    }

    public boolean isSetFactor() {
        return isSetField(228);
    }

    public void set(quickfix.field.CreditRating value) {
        setField(value);
    }

    public quickfix.field.CreditRating get(quickfix.field.CreditRating value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.CreditRating getCreditRating()
        throws FieldNotFound {
        quickfix.field.CreditRating value = new quickfix.field.CreditRating();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.CreditRating field) {
        return isSetField(field);
    }

    public boolean isSetCreditRating() {
        return isSetField(255);
    }

    public void set(quickfix.field.InstrRegistry value) {
        setField(value);
    }

    public quickfix.field.InstrRegistry get(quickfix.field.InstrRegistry value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.InstrRegistry getInstrRegistry()
        throws FieldNotFound {
        quickfix.field.InstrRegistry value = new quickfix.field.InstrRegistry();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.InstrRegistry field) {
        return isSetField(field);
    }

    public boolean isSetInstrRegistry() {
        return isSetField(543);
    }

    public void set(quickfix.field.CountryOfIssue value) {
        setField(value);
    }

    public quickfix.field.CountryOfIssue get(
        quickfix.field.CountryOfIssue value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.CountryOfIssue getCountryOfIssue()
        throws FieldNotFound {
        quickfix.field.CountryOfIssue value = new quickfix.field.CountryOfIssue();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.CountryOfIssue field) {
        return isSetField(field);
    }

    public boolean isSetCountryOfIssue() {
        return isSetField(470);
    }

    public void set(quickfix.field.StateOrProvinceOfIssue value) {
        setField(value);
    }

    public quickfix.field.StateOrProvinceOfIssue get(
        quickfix.field.StateOrProvinceOfIssue value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.StateOrProvinceOfIssue getStateOrProvinceOfIssue()
        throws FieldNotFound {
        quickfix.field.StateOrProvinceOfIssue value = new quickfix.field.StateOrProvinceOfIssue();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.StateOrProvinceOfIssue field) {
        return isSetField(field);
    }

    public boolean isSetStateOrProvinceOfIssue() {
        return isSetField(471);
    }

    public void set(quickfix.field.LocaleOfIssue value) {
        setField(value);
    }

    public quickfix.field.LocaleOfIssue get(quickfix.field.LocaleOfIssue value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.LocaleOfIssue getLocaleOfIssue()
        throws FieldNotFound {
        quickfix.field.LocaleOfIssue value = new quickfix.field.LocaleOfIssue();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.LocaleOfIssue field) {
        return isSetField(field);
    }

    public boolean isSetLocaleOfIssue() {
        return isSetField(472);
    }

    public void set(quickfix.field.RedemptionDate value) {
        setField(value);
    }

    public quickfix.field.RedemptionDate get(
        quickfix.field.RedemptionDate value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.RedemptionDate getRedemptionDate()
        throws FieldNotFound {
        quickfix.field.RedemptionDate value = new quickfix.field.RedemptionDate();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.RedemptionDate field) {
        return isSetField(field);
    }

    public boolean isSetRedemptionDate() {
        return isSetField(240);
    }

    public void set(quickfix.field.StrikePrice value) {
        setField(value);
    }

    public quickfix.field.StrikePrice get(quickfix.field.StrikePrice value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.StrikePrice getStrikePrice()
        throws FieldNotFound {
        quickfix.field.StrikePrice value = new quickfix.field.StrikePrice();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.StrikePrice field) {
        return isSetField(field);
    }

    public boolean isSetStrikePrice() {
        return isSetField(202);
    }

    public void set(quickfix.field.StrikeCurrency value) {
        setField(value);
    }

    public quickfix.field.StrikeCurrency get(
        quickfix.field.StrikeCurrency value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.StrikeCurrency getStrikeCurrency()
        throws FieldNotFound {
        quickfix.field.StrikeCurrency value = new quickfix.field.StrikeCurrency();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.StrikeCurrency field) {
        return isSetField(field);
    }

    public boolean isSetStrikeCurrency() {
        return isSetField(947);
    }

    public void set(quickfix.field.OptAttribute value) {
        setField(value);
    }

    public quickfix.field.OptAttribute get(quickfix.field.OptAttribute value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.OptAttribute getOptAttribute()
        throws FieldNotFound {
        quickfix.field.OptAttribute value = new quickfix.field.OptAttribute();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.OptAttribute field) {
        return isSetField(field);
    }

    public boolean isSetOptAttribute() {
        return isSetField(206);
    }

    public void set(quickfix.field.ContractMultiplier value) {
        setField(value);
    }

    public quickfix.field.ContractMultiplier get(
        quickfix.field.ContractMultiplier value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.ContractMultiplier getContractMultiplier()
        throws FieldNotFound {
        quickfix.field.ContractMultiplier value = new quickfix.field.ContractMultiplier();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.ContractMultiplier field) {
        return isSetField(field);
    }

    public boolean isSetContractMultiplier() {
        return isSetField(231);
    }

    public void set(quickfix.field.CouponRate value) {
        setField(value);
    }

    public quickfix.field.CouponRate get(quickfix.field.CouponRate value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.CouponRate getCouponRate() throws FieldNotFound {
        quickfix.field.CouponRate value = new quickfix.field.CouponRate();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.CouponRate field) {
        return isSetField(field);
    }

    public boolean isSetCouponRate() {
        return isSetField(223);
    }

    public void set(quickfix.field.SecurityExchange value) {
        setField(value);
    }

    public quickfix.field.SecurityExchange get(
        quickfix.field.SecurityExchange value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.SecurityExchange getSecurityExchange()
        throws FieldNotFound {
        quickfix.field.SecurityExchange value = new quickfix.field.SecurityExchange();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.SecurityExchange field) {
        return isSetField(field);
    }

    public boolean isSetSecurityExchange() {
        return isSetField(207);
    }

    public void set(quickfix.field.Issuer value) {
        setField(value);
    }

    public quickfix.field.Issuer get(quickfix.field.Issuer value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.Issuer getIssuer() throws FieldNotFound {
        quickfix.field.Issuer value = new quickfix.field.Issuer();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.Issuer field) {
        return isSetField(field);
    }

    public boolean isSetIssuer() {
        return isSetField(106);
    }

    public void set(quickfix.field.EncodedIssuerLen value) {
        setField(value);
    }

    public quickfix.field.EncodedIssuerLen get(
        quickfix.field.EncodedIssuerLen value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.EncodedIssuerLen getEncodedIssuerLen()
        throws FieldNotFound {
        quickfix.field.EncodedIssuerLen value = new quickfix.field.EncodedIssuerLen();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.EncodedIssuerLen field) {
        return isSetField(field);
    }

    public boolean isSetEncodedIssuerLen() {
        return isSetField(348);
    }

    public void set(quickfix.field.EncodedIssuer value) {
        setField(value);
    }

    public quickfix.field.EncodedIssuer get(quickfix.field.EncodedIssuer value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.EncodedIssuer getEncodedIssuer()
        throws FieldNotFound {
        quickfix.field.EncodedIssuer value = new quickfix.field.EncodedIssuer();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.EncodedIssuer field) {
        return isSetField(field);
    }

    public boolean isSetEncodedIssuer() {
        return isSetField(349);
    }

    public void set(quickfix.field.SecurityDesc value) {
        setField(value);
    }

    public quickfix.field.SecurityDesc get(quickfix.field.SecurityDesc value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.SecurityDesc getSecurityDesc()
        throws FieldNotFound {
        quickfix.field.SecurityDesc value = new quickfix.field.SecurityDesc();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.SecurityDesc field) {
        return isSetField(field);
    }

    public boolean isSetSecurityDesc() {
        return isSetField(107);
    }

    public void set(quickfix.field.EncodedSecurityDescLen value) {
        setField(value);
    }

    public quickfix.field.EncodedSecurityDescLen get(
        quickfix.field.EncodedSecurityDescLen value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.EncodedSecurityDescLen getEncodedSecurityDescLen()
        throws FieldNotFound {
        quickfix.field.EncodedSecurityDescLen value = new quickfix.field.EncodedSecurityDescLen();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.EncodedSecurityDescLen field) {
        return isSetField(field);
    }

    public boolean isSetEncodedSecurityDescLen() {
        return isSetField(350);
    }

    public void set(quickfix.field.EncodedSecurityDesc value) {
        setField(value);
    }

    public quickfix.field.EncodedSecurityDesc get(
        quickfix.field.EncodedSecurityDesc value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.EncodedSecurityDesc getEncodedSecurityDesc()
        throws FieldNotFound {
        quickfix.field.EncodedSecurityDesc value = new quickfix.field.EncodedSecurityDesc();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.EncodedSecurityDesc field) {
        return isSetField(field);
    }

    public boolean isSetEncodedSecurityDesc() {
        return isSetField(351);
    }

    public void set(quickfix.field.Pool value) {
        setField(value);
    }

    public quickfix.field.Pool get(quickfix.field.Pool value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.Pool getPool() throws FieldNotFound {
        quickfix.field.Pool value = new quickfix.field.Pool();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.Pool field) {
        return isSetField(field);
    }

    public boolean isSetPool() {
        return isSetField(691);
    }

    public void set(quickfix.field.ContractSettlMonth value) {
        setField(value);
    }

    public quickfix.field.ContractSettlMonth get(
        quickfix.field.ContractSettlMonth value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.ContractSettlMonth getContractSettlMonth()
        throws FieldNotFound {
        quickfix.field.ContractSettlMonth value = new quickfix.field.ContractSettlMonth();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.ContractSettlMonth field) {
        return isSetField(field);
    }

    public boolean isSetContractSettlMonth() {
        return isSetField(667);
    }

    public void set(quickfix.field.CPProgram value) {
        setField(value);
    }

    public quickfix.field.CPProgram get(quickfix.field.CPProgram value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.CPProgram getCPProgram() throws FieldNotFound {
        quickfix.field.CPProgram value = new quickfix.field.CPProgram();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.CPProgram field) {
        return isSetField(field);
    }

    public boolean isSetCPProgram() {
        return isSetField(875);
    }

    public void set(quickfix.field.CPRegType value) {
        setField(value);
    }

    public quickfix.field.CPRegType get(quickfix.field.CPRegType value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.CPRegType getCPRegType() throws FieldNotFound {
        quickfix.field.CPRegType value = new quickfix.field.CPRegType();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.CPRegType field) {
        return isSetField(field);
    }

    public boolean isSetCPRegType() {
        return isSetField(876);
    }

    public void set(quickfix.fix50.component.EvntGrp component) {
        setComponent(component);
    }

    public quickfix.fix50.component.EvntGrp get(
        quickfix.fix50.component.EvntGrp component) throws FieldNotFound {
        getComponent(component);

        return component;
    }

    public quickfix.fix50.component.EvntGrp getEvntGrp()
        throws FieldNotFound {
        quickfix.fix50.component.EvntGrp component = new quickfix.fix50.component.EvntGrp();
        getComponent(component);

        return component;
    }

    public void set(quickfix.field.NoEvents value) {
        setField(value);
    }

    public quickfix.field.NoEvents get(quickfix.field.NoEvents value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.NoEvents getNoEvents() throws FieldNotFound {
        quickfix.field.NoEvents value = new quickfix.field.NoEvents();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.NoEvents field) {
        return isSetField(field);
    }

    public boolean isSetNoEvents() {
        return isSetField(864);
    }

    public void set(quickfix.field.DatedDate value) {
        setField(value);
    }

    public quickfix.field.DatedDate get(quickfix.field.DatedDate value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.DatedDate getDatedDate() throws FieldNotFound {
        quickfix.field.DatedDate value = new quickfix.field.DatedDate();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.DatedDate field) {
        return isSetField(field);
    }

    public boolean isSetDatedDate() {
        return isSetField(873);
    }

    public void set(quickfix.field.InterestAccrualDate value) {
        setField(value);
    }

    public quickfix.field.InterestAccrualDate get(
        quickfix.field.InterestAccrualDate value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.InterestAccrualDate getInterestAccrualDate()
        throws FieldNotFound {
        quickfix.field.InterestAccrualDate value = new quickfix.field.InterestAccrualDate();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.InterestAccrualDate field) {
        return isSetField(field);
    }

    public boolean isSetInterestAccrualDate() {
        return isSetField(874);
    }

    public void set(quickfix.field.SecurityStatus value) {
        setField(value);
    }

    public quickfix.field.SecurityStatus get(
        quickfix.field.SecurityStatus value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.SecurityStatus getSecurityStatus()
        throws FieldNotFound {
        quickfix.field.SecurityStatus value = new quickfix.field.SecurityStatus();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.SecurityStatus field) {
        return isSetField(field);
    }

    public boolean isSetSecurityStatus() {
        return isSetField(965);
    }

    public void set(quickfix.field.SettleOnOpenFlag value) {
        setField(value);
    }

    public quickfix.field.SettleOnOpenFlag get(
        quickfix.field.SettleOnOpenFlag value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.SettleOnOpenFlag getSettleOnOpenFlag()
        throws FieldNotFound {
        quickfix.field.SettleOnOpenFlag value = new quickfix.field.SettleOnOpenFlag();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.SettleOnOpenFlag field) {
        return isSetField(field);
    }

    public boolean isSetSettleOnOpenFlag() {
        return isSetField(966);
    }

    public void set(quickfix.field.InstrmtAssignmentMethod value) {
        setField(value);
    }

    public quickfix.field.InstrmtAssignmentMethod get(
        quickfix.field.InstrmtAssignmentMethod value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.InstrmtAssignmentMethod getInstrmtAssignmentMethod()
        throws FieldNotFound {
        quickfix.field.InstrmtAssignmentMethod value = new quickfix.field.InstrmtAssignmentMethod();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.InstrmtAssignmentMethod field) {
        return isSetField(field);
    }

    public boolean isSetInstrmtAssignmentMethod() {
        return isSetField(1049);
    }

    public void set(quickfix.field.StrikeMultiplier value) {
        setField(value);
    }

    public quickfix.field.StrikeMultiplier get(
        quickfix.field.StrikeMultiplier value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.StrikeMultiplier getStrikeMultiplier()
        throws FieldNotFound {
        quickfix.field.StrikeMultiplier value = new quickfix.field.StrikeMultiplier();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.StrikeMultiplier field) {
        return isSetField(field);
    }

    public boolean isSetStrikeMultiplier() {
        return isSetField(967);
    }

    public void set(quickfix.field.StrikeValue value) {
        setField(value);
    }

    public quickfix.field.StrikeValue get(quickfix.field.StrikeValue value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.StrikeValue getStrikeValue()
        throws FieldNotFound {
        quickfix.field.StrikeValue value = new quickfix.field.StrikeValue();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.StrikeValue field) {
        return isSetField(field);
    }

    public boolean isSetStrikeValue() {
        return isSetField(968);
    }

    public void set(quickfix.field.MinPriceIncrement value) {
        setField(value);
    }

    public quickfix.field.MinPriceIncrement get(
        quickfix.field.MinPriceIncrement value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.MinPriceIncrement getMinPriceIncrement()
        throws FieldNotFound {
        quickfix.field.MinPriceIncrement value = new quickfix.field.MinPriceIncrement();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.MinPriceIncrement field) {
        return isSetField(field);
    }

    public boolean isSetMinPriceIncrement() {
        return isSetField(969);
    }

    public void set(quickfix.field.PositionLimit value) {
        setField(value);
    }

    public quickfix.field.PositionLimit get(quickfix.field.PositionLimit value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.PositionLimit getPositionLimit()
        throws FieldNotFound {
        quickfix.field.PositionLimit value = new quickfix.field.PositionLimit();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.PositionLimit field) {
        return isSetField(field);
    }

    public boolean isSetPositionLimit() {
        return isSetField(970);
    }

    public void set(quickfix.field.NTPositionLimit value) {
        setField(value);
    }

    public quickfix.field.NTPositionLimit get(
        quickfix.field.NTPositionLimit value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.NTPositionLimit getNTPositionLimit()
        throws FieldNotFound {
        quickfix.field.NTPositionLimit value = new quickfix.field.NTPositionLimit();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.NTPositionLimit field) {
        return isSetField(field);
    }

    public boolean isSetNTPositionLimit() {
        return isSetField(971);
    }

    public void set(quickfix.fix50.component.InstrumentParties component) {
        setComponent(component);
    }

    public quickfix.fix50.component.InstrumentParties get(
        quickfix.fix50.component.InstrumentParties component)
        throws FieldNotFound {
        getComponent(component);

        return component;
    }

    public quickfix.fix50.component.InstrumentParties getInstrumentParties()
        throws FieldNotFound {
        quickfix.fix50.component.InstrumentParties component = new quickfix.fix50.component.InstrumentParties();
        getComponent(component);

        return component;
    }

    public void set(quickfix.field.NoInstrumentParties value) {
        setField(value);
    }

    public quickfix.field.NoInstrumentParties get(
        quickfix.field.NoInstrumentParties value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.NoInstrumentParties getNoInstrumentParties()
        throws FieldNotFound {
        quickfix.field.NoInstrumentParties value = new quickfix.field.NoInstrumentParties();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.NoInstrumentParties field) {
        return isSetField(field);
    }

    public boolean isSetNoInstrumentParties() {
        return isSetField(1018);
    }

    public void set(quickfix.field.UnitofMeasure value) {
        setField(value);
    }

    public quickfix.field.UnitofMeasure get(quickfix.field.UnitofMeasure value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.UnitofMeasure getUnitofMeasure()
        throws FieldNotFound {
        quickfix.field.UnitofMeasure value = new quickfix.field.UnitofMeasure();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.UnitofMeasure field) {
        return isSetField(field);
    }

    public boolean isSetUnitofMeasure() {
        return isSetField(996);
    }

    public void set(quickfix.field.TimeUnit value) {
        setField(value);
    }

    public quickfix.field.TimeUnit get(quickfix.field.TimeUnit value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.TimeUnit getTimeUnit() throws FieldNotFound {
        quickfix.field.TimeUnit value = new quickfix.field.TimeUnit();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.TimeUnit field) {
        return isSetField(field);
    }

    public boolean isSetTimeUnit() {
        return isSetField(997);
    }

    public void set(quickfix.field.MaturityTime value) {
        setField(value);
    }

    public quickfix.field.MaturityTime get(quickfix.field.MaturityTime value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.MaturityTime getMaturityTime()
        throws FieldNotFound {
        quickfix.field.MaturityTime value = new quickfix.field.MaturityTime();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.MaturityTime field) {
        return isSetField(field);
    }

    public boolean isSetMaturityTime() {
        return isSetField(1079);
    }

    public void set(quickfix.fix50.component.UndInstrmtGrp component) {
        setComponent(component);
    }

    public quickfix.fix50.component.UndInstrmtGrp get(
        quickfix.fix50.component.UndInstrmtGrp component)
        throws FieldNotFound {
        getComponent(component);

        return component;
    }

    public quickfix.fix50.component.UndInstrmtGrp getUndInstrmtGrp()
        throws FieldNotFound {
        quickfix.fix50.component.UndInstrmtGrp component = new quickfix.fix50.component.UndInstrmtGrp();
        getComponent(component);

        return component;
    }

    public void set(quickfix.field.NoUnderlyings value) {
        setField(value);
    }

    public quickfix.field.NoUnderlyings get(quickfix.field.NoUnderlyings value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.NoUnderlyings getNoUnderlyings()
        throws FieldNotFound {
        quickfix.field.NoUnderlyings value = new quickfix.field.NoUnderlyings();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.NoUnderlyings field) {
        return isSetField(field);
    }

    public boolean isSetNoUnderlyings() {
        return isSetField(711);
    }

    public void set(quickfix.fix50.component.InstrmtLegGrp component) {
        setComponent(component);
    }

    public quickfix.fix50.component.InstrmtLegGrp get(
        quickfix.fix50.component.InstrmtLegGrp component)
        throws FieldNotFound {
        getComponent(component);

        return component;
    }

    public quickfix.fix50.component.InstrmtLegGrp getInstrmtLegGrp()
        throws FieldNotFound {
        quickfix.fix50.component.InstrmtLegGrp component = new quickfix.fix50.component.InstrmtLegGrp();
        getComponent(component);

        return component;
    }

    public void set(quickfix.field.NoLegs value) {
        setField(value);
    }

    public quickfix.field.NoLegs get(quickfix.field.NoLegs value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.NoLegs getNoLegs() throws FieldNotFound {
        quickfix.field.NoLegs value = new quickfix.field.NoLegs();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.NoLegs field) {
        return isSetField(field);
    }

    public boolean isSetNoLegs() {
        return isSetField(555);
    }

    public void set(quickfix.field.FinancialStatus value) {
        setField(value);
    }

    public quickfix.field.FinancialStatus get(
        quickfix.field.FinancialStatus value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.FinancialStatus getFinancialStatus()
        throws FieldNotFound {
        quickfix.field.FinancialStatus value = new quickfix.field.FinancialStatus();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.FinancialStatus field) {
        return isSetField(field);
    }

    public boolean isSetFinancialStatus() {
        return isSetField(291);
    }

    public void set(quickfix.field.CorporateAction value) {
        setField(value);
    }

    public quickfix.field.CorporateAction get(
        quickfix.field.CorporateAction value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.CorporateAction getCorporateAction()
        throws FieldNotFound {
        quickfix.field.CorporateAction value = new quickfix.field.CorporateAction();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.CorporateAction field) {
        return isSetField(field);
    }

    public boolean isSetCorporateAction() {
        return isSetField(292);
    }

    public void set(quickfix.field.NetChgPrevDay value) {
        setField(value);
    }

    public quickfix.field.NetChgPrevDay get(quickfix.field.NetChgPrevDay value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.NetChgPrevDay getNetChgPrevDay()
        throws FieldNotFound {
        quickfix.field.NetChgPrevDay value = new quickfix.field.NetChgPrevDay();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.NetChgPrevDay field) {
        return isSetField(field);
    }

    public boolean isSetNetChgPrevDay() {
        return isSetField(451);
    }

    public void set(quickfix.fix50.component.MDFullGrp component) {
        setComponent(component);
    }

    public quickfix.fix50.component.MDFullGrp get(
        quickfix.fix50.component.MDFullGrp component) throws FieldNotFound {
        getComponent(component);

        return component;
    }

    public quickfix.fix50.component.MDFullGrp getMDFullGrp()
        throws FieldNotFound {
        quickfix.fix50.component.MDFullGrp component = new quickfix.fix50.component.MDFullGrp();
        getComponent(component);

        return component;
    }

    public void set(quickfix.field.NoMDEntries value) {
        setField(value);
    }

    public quickfix.field.NoMDEntries get(quickfix.field.NoMDEntries value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.NoMDEntries getNoMDEntries()
        throws FieldNotFound {
        quickfix.field.NoMDEntries value = new quickfix.field.NoMDEntries();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.NoMDEntries field) {
        return isSetField(field);
    }

    public boolean isSetNoMDEntries() {
        return isSetField(268);
    }

    public void set(quickfix.field.ApplQueueDepth value) {
        setField(value);
    }

    public quickfix.field.ApplQueueDepth get(
        quickfix.field.ApplQueueDepth value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.ApplQueueDepth getApplQueueDepth()
        throws FieldNotFound {
        quickfix.field.ApplQueueDepth value = new quickfix.field.ApplQueueDepth();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.ApplQueueDepth field) {
        return isSetField(field);
    }

    public boolean isSetApplQueueDepth() {
        return isSetField(813);
    }

    public void set(quickfix.field.ApplQueueResolution value) {
        setField(value);
    }

    public quickfix.field.ApplQueueResolution get(
        quickfix.field.ApplQueueResolution value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.ApplQueueResolution getApplQueueResolution()
        throws FieldNotFound {
        quickfix.field.ApplQueueResolution value = new quickfix.field.ApplQueueResolution();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.ApplQueueResolution field) {
        return isSetField(field);
    }

    public boolean isSetApplQueueResolution() {
        return isSetField(814);
    }

    public void set(quickfix.field.MDReportID value) {
        setField(value);
    }

    public quickfix.field.MDReportID get(quickfix.field.MDReportID value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.MDReportID getMDReportID() throws FieldNotFound {
        quickfix.field.MDReportID value = new quickfix.field.MDReportID();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.MDReportID field) {
        return isSetField(field);
    }

    public boolean isSetMDReportID() {
        return isSetField(963);
    }

    public void set(quickfix.field.ClearingBusinessDate value) {
        setField(value);
    }

    public quickfix.field.ClearingBusinessDate get(
        quickfix.field.ClearingBusinessDate value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.ClearingBusinessDate getClearingBusinessDate()
        throws FieldNotFound {
        quickfix.field.ClearingBusinessDate value = new quickfix.field.ClearingBusinessDate();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.ClearingBusinessDate field) {
        return isSetField(field);
    }

    public boolean isSetClearingBusinessDate() {
        return isSetField(715);
    }

    public void set(quickfix.field.MDBookType value) {
        setField(value);
    }

    public quickfix.field.MDBookType get(quickfix.field.MDBookType value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.MDBookType getMDBookType() throws FieldNotFound {
        quickfix.field.MDBookType value = new quickfix.field.MDBookType();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.MDBookType field) {
        return isSetField(field);
    }

    public boolean isSetMDBookType() {
        return isSetField(1021);
    }

    public void set(quickfix.field.MDFeedType value) {
        setField(value);
    }

    public quickfix.field.MDFeedType get(quickfix.field.MDFeedType value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.MDFeedType getMDFeedType() throws FieldNotFound {
        quickfix.field.MDFeedType value = new quickfix.field.MDFeedType();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.MDFeedType field) {
        return isSetField(field);
    }

    public boolean isSetMDFeedType() {
        return isSetField(1022);
    }

    public void set(quickfix.field.TradeDate value) {
        setField(value);
    }

    public quickfix.field.TradeDate get(quickfix.field.TradeDate value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.TradeDate getTradeDate() throws FieldNotFound {
        quickfix.field.TradeDate value = new quickfix.field.TradeDate();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.TradeDate field) {
        return isSetField(field);
    }

    public boolean isSetTradeDate() {
        return isSetField(75);
    }

    public void set(quickfix.fix50.component.RoutingGrp component) {
        setComponent(component);
    }

    public quickfix.fix50.component.RoutingGrp get(
        quickfix.fix50.component.RoutingGrp component)
        throws FieldNotFound {
        getComponent(component);

        return component;
    }

    public quickfix.fix50.component.RoutingGrp getRoutingGrp()
        throws FieldNotFound {
        quickfix.fix50.component.RoutingGrp component = new quickfix.fix50.component.RoutingGrp();
        getComponent(component);

        return component;
    }

    public void set(quickfix.field.NoRoutingIDs value) {
        setField(value);
    }

    public quickfix.field.NoRoutingIDs get(quickfix.field.NoRoutingIDs value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.NoRoutingIDs getNoRoutingIDs()
        throws FieldNotFound {
        quickfix.field.NoRoutingIDs value = new quickfix.field.NoRoutingIDs();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.NoRoutingIDs field) {
        return isSetField(field);
    }

    public boolean isSetNoRoutingIDs() {
        return isSetField(215);
    }

    public static class NoSecurityAltID extends Group {
        static final long serialVersionUID = 20050617;

        public NoSecurityAltID() {
            super(454, 455, new int[] { 455, 456, 0 });
        }

        public void set(quickfix.field.SecurityAltID value) {
            setField(value);
        }

        public quickfix.field.SecurityAltID get(
            quickfix.field.SecurityAltID value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.SecurityAltID getSecurityAltID()
            throws FieldNotFound {
            quickfix.field.SecurityAltID value = new quickfix.field.SecurityAltID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.SecurityAltID field) {
            return isSetField(field);
        }

        public boolean isSetSecurityAltID() {
            return isSetField(455);
        }

        public void set(quickfix.field.SecurityAltIDSource value) {
            setField(value);
        }

        public quickfix.field.SecurityAltIDSource get(
            quickfix.field.SecurityAltIDSource value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.SecurityAltIDSource getSecurityAltIDSource()
            throws FieldNotFound {
            quickfix.field.SecurityAltIDSource value = new quickfix.field.SecurityAltIDSource();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.SecurityAltIDSource field) {
            return isSetField(field);
        }

        public boolean isSetSecurityAltIDSource() {
            return isSetField(456);
        }
    }

    public static class NoEvents extends Group {
        static final long serialVersionUID = 20050617;

        public NoEvents() {
            super(864, 865, new int[] { 865, 866, 867, 868, 0 });
        }

        public void set(quickfix.field.EventType value) {
            setField(value);
        }

        public quickfix.field.EventType get(quickfix.field.EventType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EventType getEventType()
            throws FieldNotFound {
            quickfix.field.EventType value = new quickfix.field.EventType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EventType field) {
            return isSetField(field);
        }

        public boolean isSetEventType() {
            return isSetField(865);
        }

        public void set(quickfix.field.EventDate value) {
            setField(value);
        }

        public quickfix.field.EventDate get(quickfix.field.EventDate value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EventDate getEventDate()
            throws FieldNotFound {
            quickfix.field.EventDate value = new quickfix.field.EventDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EventDate field) {
            return isSetField(field);
        }

        public boolean isSetEventDate() {
            return isSetField(866);
        }

        public void set(quickfix.field.EventPx value) {
            setField(value);
        }

        public quickfix.field.EventPx get(quickfix.field.EventPx value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EventPx getEventPx() throws FieldNotFound {
            quickfix.field.EventPx value = new quickfix.field.EventPx();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EventPx field) {
            return isSetField(field);
        }

        public boolean isSetEventPx() {
            return isSetField(867);
        }

        public void set(quickfix.field.EventText value) {
            setField(value);
        }

        public quickfix.field.EventText get(quickfix.field.EventText value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EventText getEventText()
            throws FieldNotFound {
            quickfix.field.EventText value = new quickfix.field.EventText();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EventText field) {
            return isSetField(field);
        }

        public boolean isSetEventText() {
            return isSetField(868);
        }
    }

    public static class NoInstrumentParties extends Group {
        static final long serialVersionUID = 20050617;

        public NoInstrumentParties() {
            super(1018, 1019, new int[] { 1019, 1050, 1051, 1052, 0 });
        }

        public void set(quickfix.field.InstrumentPartyID value) {
            setField(value);
        }

        public quickfix.field.InstrumentPartyID get(
            quickfix.field.InstrumentPartyID value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.InstrumentPartyID getInstrumentPartyID()
            throws FieldNotFound {
            quickfix.field.InstrumentPartyID value = new quickfix.field.InstrumentPartyID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.InstrumentPartyID field) {
            return isSetField(field);
        }

        public boolean isSetInstrumentPartyID() {
            return isSetField(1019);
        }

        public void set(quickfix.field.InstrumentPartyIDSource value) {
            setField(value);
        }

        public quickfix.field.InstrumentPartyIDSource get(
            quickfix.field.InstrumentPartyIDSource value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.InstrumentPartyIDSource getInstrumentPartyIDSource()
            throws FieldNotFound {
            quickfix.field.InstrumentPartyIDSource value = new quickfix.field.InstrumentPartyIDSource();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.InstrumentPartyIDSource field) {
            return isSetField(field);
        }

        public boolean isSetInstrumentPartyIDSource() {
            return isSetField(1050);
        }

        public void set(quickfix.field.InstrumentPartyRole value) {
            setField(value);
        }

        public quickfix.field.InstrumentPartyRole get(
            quickfix.field.InstrumentPartyRole value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.InstrumentPartyRole getInstrumentPartyRole()
            throws FieldNotFound {
            quickfix.field.InstrumentPartyRole value = new quickfix.field.InstrumentPartyRole();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.InstrumentPartyRole field) {
            return isSetField(field);
        }

        public boolean isSetInstrumentPartyRole() {
            return isSetField(1051);
        }

        public void set(quickfix.fix50.component.InstrumentPtysSubGrp component) {
            setComponent(component);
        }

        public quickfix.fix50.component.InstrumentPtysSubGrp get(
            quickfix.fix50.component.InstrumentPtysSubGrp component)
            throws FieldNotFound {
            getComponent(component);

            return component;
        }

        public quickfix.fix50.component.InstrumentPtysSubGrp getInstrumentPtysSubGrp()
            throws FieldNotFound {
            quickfix.fix50.component.InstrumentPtysSubGrp component = new quickfix.fix50.component.InstrumentPtysSubGrp();
            getComponent(component);

            return component;
        }

        public void set(quickfix.field.NoInstrumentPartySubIDs value) {
            setField(value);
        }

        public quickfix.field.NoInstrumentPartySubIDs get(
            quickfix.field.NoInstrumentPartySubIDs value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.NoInstrumentPartySubIDs getNoInstrumentPartySubIDs()
            throws FieldNotFound {
            quickfix.field.NoInstrumentPartySubIDs value = new quickfix.field.NoInstrumentPartySubIDs();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.NoInstrumentPartySubIDs field) {
            return isSetField(field);
        }

        public boolean isSetNoInstrumentPartySubIDs() {
            return isSetField(1052);
        }

        public static class NoInstrumentPartySubIDs extends Group {
            static final long serialVersionUID = 20050617;

            public NoInstrumentPartySubIDs() {
                super(1052, 1053, new int[] { 1053, 1054, 0 });
            }

            public void set(quickfix.field.InstrumentPartySubID value) {
                setField(value);
            }

            public quickfix.field.InstrumentPartySubID get(
                quickfix.field.InstrumentPartySubID value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.InstrumentPartySubID getInstrumentPartySubID()
                throws FieldNotFound {
                quickfix.field.InstrumentPartySubID value = new quickfix.field.InstrumentPartySubID();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.InstrumentPartySubID field) {
                return isSetField(field);
            }

            public boolean isSetInstrumentPartySubID() {
                return isSetField(1053);
            }

            public void set(quickfix.field.InstrumentPartySubIDType value) {
                setField(value);
            }

            public quickfix.field.InstrumentPartySubIDType get(
                quickfix.field.InstrumentPartySubIDType value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.InstrumentPartySubIDType getInstrumentPartySubIDType()
                throws FieldNotFound {
                quickfix.field.InstrumentPartySubIDType value = new quickfix.field.InstrumentPartySubIDType();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.InstrumentPartySubIDType field) {
                return isSetField(field);
            }

            public boolean isSetInstrumentPartySubIDType() {
                return isSetField(1054);
            }
        }
    }

    public static class NoUnderlyings extends Group {
        static final long serialVersionUID = 20050617;

        public NoUnderlyings() {
            super(711, 311,
                new int[] {
                    311, 312, 309, 305, 457, 462, 463, 310, 763, 313, 542, 241,
                    242, 243, 244, 245, 246, 256, 595, 592, 593, 594, 247, 316,
                    941, 317, 436, 435, 308, 306, 362, 363, 307, 364, 365, 877,
                    878, 318, 879, 810, 882, 883, 884, 885, 886, 887, 972, 975,
                    973, 974, 998, 1000, 1038, 1058, 1039, 1044, 1045, 1046, 0
                });
        }

        public void set(quickfix.fix50.component.UnderlyingInstrument component) {
            setComponent(component);
        }

        public quickfix.fix50.component.UnderlyingInstrument get(
            quickfix.fix50.component.UnderlyingInstrument component)
            throws FieldNotFound {
            getComponent(component);

            return component;
        }

        public quickfix.fix50.component.UnderlyingInstrument getUnderlyingInstrument()
            throws FieldNotFound {
            quickfix.fix50.component.UnderlyingInstrument component = new quickfix.fix50.component.UnderlyingInstrument();
            getComponent(component);

            return component;
        }

        public void set(quickfix.field.UnderlyingSymbol value) {
            setField(value);
        }

        public quickfix.field.UnderlyingSymbol get(
            quickfix.field.UnderlyingSymbol value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingSymbol getUnderlyingSymbol()
            throws FieldNotFound {
            quickfix.field.UnderlyingSymbol value = new quickfix.field.UnderlyingSymbol();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingSymbol field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingSymbol() {
            return isSetField(311);
        }

        public void set(quickfix.field.UnderlyingSymbolSfx value) {
            setField(value);
        }

        public quickfix.field.UnderlyingSymbolSfx get(
            quickfix.field.UnderlyingSymbolSfx value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingSymbolSfx getUnderlyingSymbolSfx()
            throws FieldNotFound {
            quickfix.field.UnderlyingSymbolSfx value = new quickfix.field.UnderlyingSymbolSfx();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingSymbolSfx field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingSymbolSfx() {
            return isSetField(312);
        }

        public void set(quickfix.field.UnderlyingSecurityID value) {
            setField(value);
        }

        public quickfix.field.UnderlyingSecurityID get(
            quickfix.field.UnderlyingSecurityID value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingSecurityID getUnderlyingSecurityID()
            throws FieldNotFound {
            quickfix.field.UnderlyingSecurityID value = new quickfix.field.UnderlyingSecurityID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingSecurityID field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingSecurityID() {
            return isSetField(309);
        }

        public void set(quickfix.field.UnderlyingSecurityIDSource value) {
            setField(value);
        }

        public quickfix.field.UnderlyingSecurityIDSource get(
            quickfix.field.UnderlyingSecurityIDSource value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingSecurityIDSource getUnderlyingSecurityIDSource()
            throws FieldNotFound {
            quickfix.field.UnderlyingSecurityIDSource value = new quickfix.field.UnderlyingSecurityIDSource();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingSecurityIDSource field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingSecurityIDSource() {
            return isSetField(305);
        }

        public void set(quickfix.fix50.component.UndSecAltIDGrp component) {
            setComponent(component);
        }

        public quickfix.fix50.component.UndSecAltIDGrp get(
            quickfix.fix50.component.UndSecAltIDGrp component)
            throws FieldNotFound {
            getComponent(component);

            return component;
        }

        public quickfix.fix50.component.UndSecAltIDGrp getUndSecAltIDGrp()
            throws FieldNotFound {
            quickfix.fix50.component.UndSecAltIDGrp component = new quickfix.fix50.component.UndSecAltIDGrp();
            getComponent(component);

            return component;
        }

        public void set(quickfix.field.NoUnderlyingSecurityAltID value) {
            setField(value);
        }

        public quickfix.field.NoUnderlyingSecurityAltID get(
            quickfix.field.NoUnderlyingSecurityAltID value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.NoUnderlyingSecurityAltID getNoUnderlyingSecurityAltID()
            throws FieldNotFound {
            quickfix.field.NoUnderlyingSecurityAltID value = new quickfix.field.NoUnderlyingSecurityAltID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.NoUnderlyingSecurityAltID field) {
            return isSetField(field);
        }

        public boolean isSetNoUnderlyingSecurityAltID() {
            return isSetField(457);
        }

        public void set(quickfix.field.UnderlyingProduct value) {
            setField(value);
        }

        public quickfix.field.UnderlyingProduct get(
            quickfix.field.UnderlyingProduct value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingProduct getUnderlyingProduct()
            throws FieldNotFound {
            quickfix.field.UnderlyingProduct value = new quickfix.field.UnderlyingProduct();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingProduct field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingProduct() {
            return isSetField(462);
        }

        public void set(quickfix.field.UnderlyingCFICode value) {
            setField(value);
        }

        public quickfix.field.UnderlyingCFICode get(
            quickfix.field.UnderlyingCFICode value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingCFICode getUnderlyingCFICode()
            throws FieldNotFound {
            quickfix.field.UnderlyingCFICode value = new quickfix.field.UnderlyingCFICode();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingCFICode field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingCFICode() {
            return isSetField(463);
        }

        public void set(quickfix.field.UnderlyingSecurityType value) {
            setField(value);
        }

        public quickfix.field.UnderlyingSecurityType get(
            quickfix.field.UnderlyingSecurityType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingSecurityType getUnderlyingSecurityType()
            throws FieldNotFound {
            quickfix.field.UnderlyingSecurityType value = new quickfix.field.UnderlyingSecurityType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingSecurityType field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingSecurityType() {
            return isSetField(310);
        }

        public void set(quickfix.field.UnderlyingSecuritySubType value) {
            setField(value);
        }

        public quickfix.field.UnderlyingSecuritySubType get(
            quickfix.field.UnderlyingSecuritySubType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingSecuritySubType getUnderlyingSecuritySubType()
            throws FieldNotFound {
            quickfix.field.UnderlyingSecuritySubType value = new quickfix.field.UnderlyingSecuritySubType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingSecuritySubType field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingSecuritySubType() {
            return isSetField(763);
        }

        public void set(quickfix.field.UnderlyingMaturityMonthYear value) {
            setField(value);
        }

        public quickfix.field.UnderlyingMaturityMonthYear get(
            quickfix.field.UnderlyingMaturityMonthYear value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingMaturityMonthYear getUnderlyingMaturityMonthYear()
            throws FieldNotFound {
            quickfix.field.UnderlyingMaturityMonthYear value = new quickfix.field.UnderlyingMaturityMonthYear();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingMaturityMonthYear field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingMaturityMonthYear() {
            return isSetField(313);
        }

        public void set(quickfix.field.UnderlyingMaturityDate value) {
            setField(value);
        }

        public quickfix.field.UnderlyingMaturityDate get(
            quickfix.field.UnderlyingMaturityDate value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingMaturityDate getUnderlyingMaturityDate()
            throws FieldNotFound {
            quickfix.field.UnderlyingMaturityDate value = new quickfix.field.UnderlyingMaturityDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingMaturityDate field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingMaturityDate() {
            return isSetField(542);
        }

        public void set(quickfix.field.UnderlyingCouponPaymentDate value) {
            setField(value);
        }

        public quickfix.field.UnderlyingCouponPaymentDate get(
            quickfix.field.UnderlyingCouponPaymentDate value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingCouponPaymentDate getUnderlyingCouponPaymentDate()
            throws FieldNotFound {
            quickfix.field.UnderlyingCouponPaymentDate value = new quickfix.field.UnderlyingCouponPaymentDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingCouponPaymentDate field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingCouponPaymentDate() {
            return isSetField(241);
        }

        public void set(quickfix.field.UnderlyingIssueDate value) {
            setField(value);
        }

        public quickfix.field.UnderlyingIssueDate get(
            quickfix.field.UnderlyingIssueDate value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingIssueDate getUnderlyingIssueDate()
            throws FieldNotFound {
            quickfix.field.UnderlyingIssueDate value = new quickfix.field.UnderlyingIssueDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingIssueDate field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingIssueDate() {
            return isSetField(242);
        }

        public void set(
            quickfix.field.UnderlyingRepoCollateralSecurityType value) {
            setField(value);
        }

        public quickfix.field.UnderlyingRepoCollateralSecurityType get(
            quickfix.field.UnderlyingRepoCollateralSecurityType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingRepoCollateralSecurityType getUnderlyingRepoCollateralSecurityType()
            throws FieldNotFound {
            quickfix.field.UnderlyingRepoCollateralSecurityType value = new quickfix.field.UnderlyingRepoCollateralSecurityType();
            getField(value);

            return value;
        }

        public boolean isSet(
            quickfix.field.UnderlyingRepoCollateralSecurityType field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingRepoCollateralSecurityType() {
            return isSetField(243);
        }

        public void set(quickfix.field.UnderlyingRepurchaseTerm value) {
            setField(value);
        }

        public quickfix.field.UnderlyingRepurchaseTerm get(
            quickfix.field.UnderlyingRepurchaseTerm value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingRepurchaseTerm getUnderlyingRepurchaseTerm()
            throws FieldNotFound {
            quickfix.field.UnderlyingRepurchaseTerm value = new quickfix.field.UnderlyingRepurchaseTerm();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingRepurchaseTerm field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingRepurchaseTerm() {
            return isSetField(244);
        }

        public void set(quickfix.field.UnderlyingRepurchaseRate value) {
            setField(value);
        }

        public quickfix.field.UnderlyingRepurchaseRate get(
            quickfix.field.UnderlyingRepurchaseRate value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingRepurchaseRate getUnderlyingRepurchaseRate()
            throws FieldNotFound {
            quickfix.field.UnderlyingRepurchaseRate value = new quickfix.field.UnderlyingRepurchaseRate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingRepurchaseRate field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingRepurchaseRate() {
            return isSetField(245);
        }

        public void set(quickfix.field.UnderlyingFactor value) {
            setField(value);
        }

        public quickfix.field.UnderlyingFactor get(
            quickfix.field.UnderlyingFactor value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingFactor getUnderlyingFactor()
            throws FieldNotFound {
            quickfix.field.UnderlyingFactor value = new quickfix.field.UnderlyingFactor();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingFactor field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingFactor() {
            return isSetField(246);
        }

        public void set(quickfix.field.UnderlyingCreditRating value) {
            setField(value);
        }

        public quickfix.field.UnderlyingCreditRating get(
            quickfix.field.UnderlyingCreditRating value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingCreditRating getUnderlyingCreditRating()
            throws FieldNotFound {
            quickfix.field.UnderlyingCreditRating value = new quickfix.field.UnderlyingCreditRating();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingCreditRating field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingCreditRating() {
            return isSetField(256);
        }

        public void set(quickfix.field.UnderlyingInstrRegistry value) {
            setField(value);
        }

        public quickfix.field.UnderlyingInstrRegistry get(
            quickfix.field.UnderlyingInstrRegistry value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingInstrRegistry getUnderlyingInstrRegistry()
            throws FieldNotFound {
            quickfix.field.UnderlyingInstrRegistry value = new quickfix.field.UnderlyingInstrRegistry();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingInstrRegistry field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingInstrRegistry() {
            return isSetField(595);
        }

        public void set(quickfix.field.UnderlyingCountryOfIssue value) {
            setField(value);
        }

        public quickfix.field.UnderlyingCountryOfIssue get(
            quickfix.field.UnderlyingCountryOfIssue value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingCountryOfIssue getUnderlyingCountryOfIssue()
            throws FieldNotFound {
            quickfix.field.UnderlyingCountryOfIssue value = new quickfix.field.UnderlyingCountryOfIssue();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingCountryOfIssue field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingCountryOfIssue() {
            return isSetField(592);
        }

        public void set(quickfix.field.UnderlyingStateOrProvinceOfIssue value) {
            setField(value);
        }

        public quickfix.field.UnderlyingStateOrProvinceOfIssue get(
            quickfix.field.UnderlyingStateOrProvinceOfIssue value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingStateOrProvinceOfIssue getUnderlyingStateOrProvinceOfIssue()
            throws FieldNotFound {
            quickfix.field.UnderlyingStateOrProvinceOfIssue value = new quickfix.field.UnderlyingStateOrProvinceOfIssue();
            getField(value);

            return value;
        }

        public boolean isSet(
            quickfix.field.UnderlyingStateOrProvinceOfIssue field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingStateOrProvinceOfIssue() {
            return isSetField(593);
        }

        public void set(quickfix.field.UnderlyingLocaleOfIssue value) {
            setField(value);
        }

        public quickfix.field.UnderlyingLocaleOfIssue get(
            quickfix.field.UnderlyingLocaleOfIssue value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingLocaleOfIssue getUnderlyingLocaleOfIssue()
            throws FieldNotFound {
            quickfix.field.UnderlyingLocaleOfIssue value = new quickfix.field.UnderlyingLocaleOfIssue();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingLocaleOfIssue field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingLocaleOfIssue() {
            return isSetField(594);
        }

        public void set(quickfix.field.UnderlyingRedemptionDate value) {
            setField(value);
        }

        public quickfix.field.UnderlyingRedemptionDate get(
            quickfix.field.UnderlyingRedemptionDate value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingRedemptionDate getUnderlyingRedemptionDate()
            throws FieldNotFound {
            quickfix.field.UnderlyingRedemptionDate value = new quickfix.field.UnderlyingRedemptionDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingRedemptionDate field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingRedemptionDate() {
            return isSetField(247);
        }

        public void set(quickfix.field.UnderlyingStrikePrice value) {
            setField(value);
        }

        public quickfix.field.UnderlyingStrikePrice get(
            quickfix.field.UnderlyingStrikePrice value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingStrikePrice getUnderlyingStrikePrice()
            throws FieldNotFound {
            quickfix.field.UnderlyingStrikePrice value = new quickfix.field.UnderlyingStrikePrice();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingStrikePrice field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingStrikePrice() {
            return isSetField(316);
        }

        public void set(quickfix.field.UnderlyingStrikeCurrency value) {
            setField(value);
        }

        public quickfix.field.UnderlyingStrikeCurrency get(
            quickfix.field.UnderlyingStrikeCurrency value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingStrikeCurrency getUnderlyingStrikeCurrency()
            throws FieldNotFound {
            quickfix.field.UnderlyingStrikeCurrency value = new quickfix.field.UnderlyingStrikeCurrency();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingStrikeCurrency field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingStrikeCurrency() {
            return isSetField(941);
        }

        public void set(quickfix.field.UnderlyingOptAttribute value) {
            setField(value);
        }

        public quickfix.field.UnderlyingOptAttribute get(
            quickfix.field.UnderlyingOptAttribute value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingOptAttribute getUnderlyingOptAttribute()
            throws FieldNotFound {
            quickfix.field.UnderlyingOptAttribute value = new quickfix.field.UnderlyingOptAttribute();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingOptAttribute field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingOptAttribute() {
            return isSetField(317);
        }

        public void set(quickfix.field.UnderlyingContractMultiplier value) {
            setField(value);
        }

        public quickfix.field.UnderlyingContractMultiplier get(
            quickfix.field.UnderlyingContractMultiplier value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingContractMultiplier getUnderlyingContractMultiplier()
            throws FieldNotFound {
            quickfix.field.UnderlyingContractMultiplier value = new quickfix.field.UnderlyingContractMultiplier();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingContractMultiplier field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingContractMultiplier() {
            return isSetField(436);
        }

        public void set(quickfix.field.UnderlyingCouponRate value) {
            setField(value);
        }

        public quickfix.field.UnderlyingCouponRate get(
            quickfix.field.UnderlyingCouponRate value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingCouponRate getUnderlyingCouponRate()
            throws FieldNotFound {
            quickfix.field.UnderlyingCouponRate value = new quickfix.field.UnderlyingCouponRate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingCouponRate field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingCouponRate() {
            return isSetField(435);
        }

        public void set(quickfix.field.UnderlyingSecurityExchange value) {
            setField(value);
        }

        public quickfix.field.UnderlyingSecurityExchange get(
            quickfix.field.UnderlyingSecurityExchange value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingSecurityExchange getUnderlyingSecurityExchange()
            throws FieldNotFound {
            quickfix.field.UnderlyingSecurityExchange value = new quickfix.field.UnderlyingSecurityExchange();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingSecurityExchange field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingSecurityExchange() {
            return isSetField(308);
        }

        public void set(quickfix.field.UnderlyingIssuer value) {
            setField(value);
        }

        public quickfix.field.UnderlyingIssuer get(
            quickfix.field.UnderlyingIssuer value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingIssuer getUnderlyingIssuer()
            throws FieldNotFound {
            quickfix.field.UnderlyingIssuer value = new quickfix.field.UnderlyingIssuer();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingIssuer field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingIssuer() {
            return isSetField(306);
        }

        public void set(quickfix.field.EncodedUnderlyingIssuerLen value) {
            setField(value);
        }

        public quickfix.field.EncodedUnderlyingIssuerLen get(
            quickfix.field.EncodedUnderlyingIssuerLen value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EncodedUnderlyingIssuerLen getEncodedUnderlyingIssuerLen()
            throws FieldNotFound {
            quickfix.field.EncodedUnderlyingIssuerLen value = new quickfix.field.EncodedUnderlyingIssuerLen();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EncodedUnderlyingIssuerLen field) {
            return isSetField(field);
        }

        public boolean isSetEncodedUnderlyingIssuerLen() {
            return isSetField(362);
        }

        public void set(quickfix.field.EncodedUnderlyingIssuer value) {
            setField(value);
        }

        public quickfix.field.EncodedUnderlyingIssuer get(
            quickfix.field.EncodedUnderlyingIssuer value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EncodedUnderlyingIssuer getEncodedUnderlyingIssuer()
            throws FieldNotFound {
            quickfix.field.EncodedUnderlyingIssuer value = new quickfix.field.EncodedUnderlyingIssuer();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EncodedUnderlyingIssuer field) {
            return isSetField(field);
        }

        public boolean isSetEncodedUnderlyingIssuer() {
            return isSetField(363);
        }

        public void set(quickfix.field.UnderlyingSecurityDesc value) {
            setField(value);
        }

        public quickfix.field.UnderlyingSecurityDesc get(
            quickfix.field.UnderlyingSecurityDesc value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingSecurityDesc getUnderlyingSecurityDesc()
            throws FieldNotFound {
            quickfix.field.UnderlyingSecurityDesc value = new quickfix.field.UnderlyingSecurityDesc();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingSecurityDesc field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingSecurityDesc() {
            return isSetField(307);
        }

        public void set(quickfix.field.EncodedUnderlyingSecurityDescLen value) {
            setField(value);
        }

        public quickfix.field.EncodedUnderlyingSecurityDescLen get(
            quickfix.field.EncodedUnderlyingSecurityDescLen value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EncodedUnderlyingSecurityDescLen getEncodedUnderlyingSecurityDescLen()
            throws FieldNotFound {
            quickfix.field.EncodedUnderlyingSecurityDescLen value = new quickfix.field.EncodedUnderlyingSecurityDescLen();
            getField(value);

            return value;
        }

        public boolean isSet(
            quickfix.field.EncodedUnderlyingSecurityDescLen field) {
            return isSetField(field);
        }

        public boolean isSetEncodedUnderlyingSecurityDescLen() {
            return isSetField(364);
        }

        public void set(quickfix.field.EncodedUnderlyingSecurityDesc value) {
            setField(value);
        }

        public quickfix.field.EncodedUnderlyingSecurityDesc get(
            quickfix.field.EncodedUnderlyingSecurityDesc value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EncodedUnderlyingSecurityDesc getEncodedUnderlyingSecurityDesc()
            throws FieldNotFound {
            quickfix.field.EncodedUnderlyingSecurityDesc value = new quickfix.field.EncodedUnderlyingSecurityDesc();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EncodedUnderlyingSecurityDesc field) {
            return isSetField(field);
        }

        public boolean isSetEncodedUnderlyingSecurityDesc() {
            return isSetField(365);
        }

        public void set(quickfix.field.UnderlyingCPProgram value) {
            setField(value);
        }

        public quickfix.field.UnderlyingCPProgram get(
            quickfix.field.UnderlyingCPProgram value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingCPProgram getUnderlyingCPProgram()
            throws FieldNotFound {
            quickfix.field.UnderlyingCPProgram value = new quickfix.field.UnderlyingCPProgram();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingCPProgram field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingCPProgram() {
            return isSetField(877);
        }

        public void set(quickfix.field.UnderlyingCPRegType value) {
            setField(value);
        }

        public quickfix.field.UnderlyingCPRegType get(
            quickfix.field.UnderlyingCPRegType value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingCPRegType getUnderlyingCPRegType()
            throws FieldNotFound {
            quickfix.field.UnderlyingCPRegType value = new quickfix.field.UnderlyingCPRegType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingCPRegType field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingCPRegType() {
            return isSetField(878);
        }

        public void set(quickfix.field.UnderlyingCurrency value) {
            setField(value);
        }

        public quickfix.field.UnderlyingCurrency get(
            quickfix.field.UnderlyingCurrency value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingCurrency getUnderlyingCurrency()
            throws FieldNotFound {
            quickfix.field.UnderlyingCurrency value = new quickfix.field.UnderlyingCurrency();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingCurrency field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingCurrency() {
            return isSetField(318);
        }

        public void set(quickfix.field.UnderlyingQty value) {
            setField(value);
        }

        public quickfix.field.UnderlyingQty get(
            quickfix.field.UnderlyingQty value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingQty getUnderlyingQty()
            throws FieldNotFound {
            quickfix.field.UnderlyingQty value = new quickfix.field.UnderlyingQty();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingQty field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingQty() {
            return isSetField(879);
        }

        public void set(quickfix.field.UnderlyingPx value) {
            setField(value);
        }

        public quickfix.field.UnderlyingPx get(
            quickfix.field.UnderlyingPx value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingPx getUnderlyingPx()
            throws FieldNotFound {
            quickfix.field.UnderlyingPx value = new quickfix.field.UnderlyingPx();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingPx field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingPx() {
            return isSetField(810);
        }

        public void set(quickfix.field.UnderlyingDirtyPrice value) {
            setField(value);
        }

        public quickfix.field.UnderlyingDirtyPrice get(
            quickfix.field.UnderlyingDirtyPrice value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingDirtyPrice getUnderlyingDirtyPrice()
            throws FieldNotFound {
            quickfix.field.UnderlyingDirtyPrice value = new quickfix.field.UnderlyingDirtyPrice();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingDirtyPrice field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingDirtyPrice() {
            return isSetField(882);
        }

        public void set(quickfix.field.UnderlyingEndPrice value) {
            setField(value);
        }

        public quickfix.field.UnderlyingEndPrice get(
            quickfix.field.UnderlyingEndPrice value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingEndPrice getUnderlyingEndPrice()
            throws FieldNotFound {
            quickfix.field.UnderlyingEndPrice value = new quickfix.field.UnderlyingEndPrice();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingEndPrice field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingEndPrice() {
            return isSetField(883);
        }

        public void set(quickfix.field.UnderlyingStartValue value) {
            setField(value);
        }

        public quickfix.field.UnderlyingStartValue get(
            quickfix.field.UnderlyingStartValue value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingStartValue getUnderlyingStartValue()
            throws FieldNotFound {
            quickfix.field.UnderlyingStartValue value = new quickfix.field.UnderlyingStartValue();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingStartValue field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingStartValue() {
            return isSetField(884);
        }

        public void set(quickfix.field.UnderlyingCurrentValue value) {
            setField(value);
        }

        public quickfix.field.UnderlyingCurrentValue get(
            quickfix.field.UnderlyingCurrentValue value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingCurrentValue getUnderlyingCurrentValue()
            throws FieldNotFound {
            quickfix.field.UnderlyingCurrentValue value = new quickfix.field.UnderlyingCurrentValue();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingCurrentValue field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingCurrentValue() {
            return isSetField(885);
        }

        public void set(quickfix.field.UnderlyingEndValue value) {
            setField(value);
        }

        public quickfix.field.UnderlyingEndValue get(
            quickfix.field.UnderlyingEndValue value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingEndValue getUnderlyingEndValue()
            throws FieldNotFound {
            quickfix.field.UnderlyingEndValue value = new quickfix.field.UnderlyingEndValue();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingEndValue field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingEndValue() {
            return isSetField(886);
        }

        public void set(
            quickfix.fix50.component.UnderlyingStipulations component) {
            setComponent(component);
        }

        public quickfix.fix50.component.UnderlyingStipulations get(
            quickfix.fix50.component.UnderlyingStipulations component)
            throws FieldNotFound {
            getComponent(component);

            return component;
        }

        public quickfix.fix50.component.UnderlyingStipulations getUnderlyingStipulations()
            throws FieldNotFound {
            quickfix.fix50.component.UnderlyingStipulations component = new quickfix.fix50.component.UnderlyingStipulations();
            getComponent(component);

            return component;
        }

        public void set(quickfix.field.NoUnderlyingStips value) {
            setField(value);
        }

        public quickfix.field.NoUnderlyingStips get(
            quickfix.field.NoUnderlyingStips value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.NoUnderlyingStips getNoUnderlyingStips()
            throws FieldNotFound {
            quickfix.field.NoUnderlyingStips value = new quickfix.field.NoUnderlyingStips();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.NoUnderlyingStips field) {
            return isSetField(field);
        }

        public boolean isSetNoUnderlyingStips() {
            return isSetField(887);
        }

        public void set(quickfix.field.UnderlyingAllocationPercent value) {
            setField(value);
        }

        public quickfix.field.UnderlyingAllocationPercent get(
            quickfix.field.UnderlyingAllocationPercent value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingAllocationPercent getUnderlyingAllocationPercent()
            throws FieldNotFound {
            quickfix.field.UnderlyingAllocationPercent value = new quickfix.field.UnderlyingAllocationPercent();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingAllocationPercent field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingAllocationPercent() {
            return isSetField(972);
        }

        public void set(quickfix.field.UnderlyingSettlementType value) {
            setField(value);
        }

        public quickfix.field.UnderlyingSettlementType get(
            quickfix.field.UnderlyingSettlementType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingSettlementType getUnderlyingSettlementType()
            throws FieldNotFound {
            quickfix.field.UnderlyingSettlementType value = new quickfix.field.UnderlyingSettlementType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingSettlementType field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingSettlementType() {
            return isSetField(975);
        }

        public void set(quickfix.field.UnderlyingCashAmount value) {
            setField(value);
        }

        public quickfix.field.UnderlyingCashAmount get(
            quickfix.field.UnderlyingCashAmount value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingCashAmount getUnderlyingCashAmount()
            throws FieldNotFound {
            quickfix.field.UnderlyingCashAmount value = new quickfix.field.UnderlyingCashAmount();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingCashAmount field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingCashAmount() {
            return isSetField(973);
        }

        public void set(quickfix.field.UnderlyingCashType value) {
            setField(value);
        }

        public quickfix.field.UnderlyingCashType get(
            quickfix.field.UnderlyingCashType value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingCashType getUnderlyingCashType()
            throws FieldNotFound {
            quickfix.field.UnderlyingCashType value = new quickfix.field.UnderlyingCashType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingCashType field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingCashType() {
            return isSetField(974);
        }

        public void set(quickfix.field.UnderlyingUnitofMeasure value) {
            setField(value);
        }

        public quickfix.field.UnderlyingUnitofMeasure get(
            quickfix.field.UnderlyingUnitofMeasure value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingUnitofMeasure getUnderlyingUnitofMeasure()
            throws FieldNotFound {
            quickfix.field.UnderlyingUnitofMeasure value = new quickfix.field.UnderlyingUnitofMeasure();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingUnitofMeasure field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingUnitofMeasure() {
            return isSetField(998);
        }

        public void set(quickfix.field.UnderlyingTimeUnit value) {
            setField(value);
        }

        public quickfix.field.UnderlyingTimeUnit get(
            quickfix.field.UnderlyingTimeUnit value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingTimeUnit getUnderlyingTimeUnit()
            throws FieldNotFound {
            quickfix.field.UnderlyingTimeUnit value = new quickfix.field.UnderlyingTimeUnit();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingTimeUnit field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingTimeUnit() {
            return isSetField(1000);
        }

        public void set(quickfix.field.UnderlyingCapValue value) {
            setField(value);
        }

        public quickfix.field.UnderlyingCapValue get(
            quickfix.field.UnderlyingCapValue value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingCapValue getUnderlyingCapValue()
            throws FieldNotFound {
            quickfix.field.UnderlyingCapValue value = new quickfix.field.UnderlyingCapValue();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingCapValue field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingCapValue() {
            return isSetField(1038);
        }

        public void set(
            quickfix.fix50.component.UndlyInstrumentParties component) {
            setComponent(component);
        }

        public quickfix.fix50.component.UndlyInstrumentParties get(
            quickfix.fix50.component.UndlyInstrumentParties component)
            throws FieldNotFound {
            getComponent(component);

            return component;
        }

        public quickfix.fix50.component.UndlyInstrumentParties getUndlyInstrumentParties()
            throws FieldNotFound {
            quickfix.fix50.component.UndlyInstrumentParties component = new quickfix.fix50.component.UndlyInstrumentParties();
            getComponent(component);

            return component;
        }

        public void set(quickfix.field.NoUndlyInstrumentParties value) {
            setField(value);
        }

        public quickfix.field.NoUndlyInstrumentParties get(
            quickfix.field.NoUndlyInstrumentParties value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.NoUndlyInstrumentParties getNoUndlyInstrumentParties()
            throws FieldNotFound {
            quickfix.field.NoUndlyInstrumentParties value = new quickfix.field.NoUndlyInstrumentParties();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.NoUndlyInstrumentParties field) {
            return isSetField(field);
        }

        public boolean isSetNoUndlyInstrumentParties() {
            return isSetField(1058);
        }

        public void set(quickfix.field.UnderlyingSettlMethod value) {
            setField(value);
        }

        public quickfix.field.UnderlyingSettlMethod get(
            quickfix.field.UnderlyingSettlMethod value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingSettlMethod getUnderlyingSettlMethod()
            throws FieldNotFound {
            quickfix.field.UnderlyingSettlMethod value = new quickfix.field.UnderlyingSettlMethod();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingSettlMethod field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingSettlMethod() {
            return isSetField(1039);
        }

        public void set(quickfix.field.UnderlyingAdjustedQuantity value) {
            setField(value);
        }

        public quickfix.field.UnderlyingAdjustedQuantity get(
            quickfix.field.UnderlyingAdjustedQuantity value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingAdjustedQuantity getUnderlyingAdjustedQuantity()
            throws FieldNotFound {
            quickfix.field.UnderlyingAdjustedQuantity value = new quickfix.field.UnderlyingAdjustedQuantity();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingAdjustedQuantity field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingAdjustedQuantity() {
            return isSetField(1044);
        }

        public void set(quickfix.field.UnderlyingFXRate value) {
            setField(value);
        }

        public quickfix.field.UnderlyingFXRate get(
            quickfix.field.UnderlyingFXRate value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingFXRate getUnderlyingFXRate()
            throws FieldNotFound {
            quickfix.field.UnderlyingFXRate value = new quickfix.field.UnderlyingFXRate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingFXRate field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingFXRate() {
            return isSetField(1045);
        }

        public void set(quickfix.field.UnderlyingFXRateCalc value) {
            setField(value);
        }

        public quickfix.field.UnderlyingFXRateCalc get(
            quickfix.field.UnderlyingFXRateCalc value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingFXRateCalc getUnderlyingFXRateCalc()
            throws FieldNotFound {
            quickfix.field.UnderlyingFXRateCalc value = new quickfix.field.UnderlyingFXRateCalc();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingFXRateCalc field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingFXRateCalc() {
            return isSetField(1046);
        }

        public static class NoUnderlyingSecurityAltID extends Group {
            static final long serialVersionUID = 20050617;

            public NoUnderlyingSecurityAltID() {
                super(457, 458, new int[] { 458, 459, 0 });
            }

            public void set(quickfix.field.UnderlyingSecurityAltID value) {
                setField(value);
            }

            public quickfix.field.UnderlyingSecurityAltID get(
                quickfix.field.UnderlyingSecurityAltID value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.UnderlyingSecurityAltID getUnderlyingSecurityAltID()
                throws FieldNotFound {
                quickfix.field.UnderlyingSecurityAltID value = new quickfix.field.UnderlyingSecurityAltID();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.UnderlyingSecurityAltID field) {
                return isSetField(field);
            }

            public boolean isSetUnderlyingSecurityAltID() {
                return isSetField(458);
            }

            public void set(quickfix.field.UnderlyingSecurityAltIDSource value) {
                setField(value);
            }

            public quickfix.field.UnderlyingSecurityAltIDSource get(
                quickfix.field.UnderlyingSecurityAltIDSource value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.UnderlyingSecurityAltIDSource getUnderlyingSecurityAltIDSource()
                throws FieldNotFound {
                quickfix.field.UnderlyingSecurityAltIDSource value = new quickfix.field.UnderlyingSecurityAltIDSource();
                getField(value);

                return value;
            }

            public boolean isSet(
                quickfix.field.UnderlyingSecurityAltIDSource field) {
                return isSetField(field);
            }

            public boolean isSetUnderlyingSecurityAltIDSource() {
                return isSetField(459);
            }
        }

        public static class NoUnderlyingStips extends Group {
            static final long serialVersionUID = 20050617;

            public NoUnderlyingStips() {
                super(887, 888, new int[] { 888, 889, 0 });
            }

            public void set(quickfix.field.UnderlyingStipType value) {
                setField(value);
            }

            public quickfix.field.UnderlyingStipType get(
                quickfix.field.UnderlyingStipType value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.UnderlyingStipType getUnderlyingStipType()
                throws FieldNotFound {
                quickfix.field.UnderlyingStipType value = new quickfix.field.UnderlyingStipType();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.UnderlyingStipType field) {
                return isSetField(field);
            }

            public boolean isSetUnderlyingStipType() {
                return isSetField(888);
            }

            public void set(quickfix.field.UnderlyingStipValue value) {
                setField(value);
            }

            public quickfix.field.UnderlyingStipValue get(
                quickfix.field.UnderlyingStipValue value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.UnderlyingStipValue getUnderlyingStipValue()
                throws FieldNotFound {
                quickfix.field.UnderlyingStipValue value = new quickfix.field.UnderlyingStipValue();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.UnderlyingStipValue field) {
                return isSetField(field);
            }

            public boolean isSetUnderlyingStipValue() {
                return isSetField(889);
            }
        }

        public static class NoUndlyInstrumentParties extends Group {
            static final long serialVersionUID = 20050617;

            public NoUndlyInstrumentParties() {
                super(1058, 1059, new int[] { 1059, 1060, 1061, 1062, 0 });
            }

            public void set(quickfix.field.UndlyInstrumentPartyID value) {
                setField(value);
            }

            public quickfix.field.UndlyInstrumentPartyID get(
                quickfix.field.UndlyInstrumentPartyID value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.UndlyInstrumentPartyID getUndlyInstrumentPartyID()
                throws FieldNotFound {
                quickfix.field.UndlyInstrumentPartyID value = new quickfix.field.UndlyInstrumentPartyID();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.UndlyInstrumentPartyID field) {
                return isSetField(field);
            }

            public boolean isSetUndlyInstrumentPartyID() {
                return isSetField(1059);
            }

            public void set(quickfix.field.UndlyInstrumentPartyIDSource value) {
                setField(value);
            }

            public quickfix.field.UndlyInstrumentPartyIDSource get(
                quickfix.field.UndlyInstrumentPartyIDSource value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.UndlyInstrumentPartyIDSource getUndlyInstrumentPartyIDSource()
                throws FieldNotFound {
                quickfix.field.UndlyInstrumentPartyIDSource value = new quickfix.field.UndlyInstrumentPartyIDSource();
                getField(value);

                return value;
            }

            public boolean isSet(
                quickfix.field.UndlyInstrumentPartyIDSource field) {
                return isSetField(field);
            }

            public boolean isSetUndlyInstrumentPartyIDSource() {
                return isSetField(1060);
            }

            public void set(quickfix.field.UndlyInstrumentPartyRole value) {
                setField(value);
            }

            public quickfix.field.UndlyInstrumentPartyRole get(
                quickfix.field.UndlyInstrumentPartyRole value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.UndlyInstrumentPartyRole getUndlyInstrumentPartyRole()
                throws FieldNotFound {
                quickfix.field.UndlyInstrumentPartyRole value = new quickfix.field.UndlyInstrumentPartyRole();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.UndlyInstrumentPartyRole field) {
                return isSetField(field);
            }

            public boolean isSetUndlyInstrumentPartyRole() {
                return isSetField(1061);
            }

            public void set(
                quickfix.fix50.component.UndlyInstrumentPtysSubGrp component) {
                setComponent(component);
            }

            public quickfix.fix50.component.UndlyInstrumentPtysSubGrp get(
                quickfix.fix50.component.UndlyInstrumentPtysSubGrp component)
                throws FieldNotFound {
                getComponent(component);

                return component;
            }

            public quickfix.fix50.component.UndlyInstrumentPtysSubGrp getUndlyInstrumentPtysSubGrp()
                throws FieldNotFound {
                quickfix.fix50.component.UndlyInstrumentPtysSubGrp component = new quickfix.fix50.component.UndlyInstrumentPtysSubGrp();
                getComponent(component);

                return component;
            }

            public void set(quickfix.field.NoUndlyInstrumentPartySubIDs value) {
                setField(value);
            }

            public quickfix.field.NoUndlyInstrumentPartySubIDs get(
                quickfix.field.NoUndlyInstrumentPartySubIDs value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.NoUndlyInstrumentPartySubIDs getNoUndlyInstrumentPartySubIDs()
                throws FieldNotFound {
                quickfix.field.NoUndlyInstrumentPartySubIDs value = new quickfix.field.NoUndlyInstrumentPartySubIDs();
                getField(value);

                return value;
            }

            public boolean isSet(
                quickfix.field.NoUndlyInstrumentPartySubIDs field) {
                return isSetField(field);
            }

            public boolean isSetNoUndlyInstrumentPartySubIDs() {
                return isSetField(1062);
            }

            public static class NoUndlyInstrumentPartySubIDs extends Group {
                static final long serialVersionUID = 20050617;

                public NoUndlyInstrumentPartySubIDs() {
                    super(1062, 1063, new int[] { 1063, 1064, 0 });
                }

                public void set(quickfix.field.UndlyInstrumentPartySubID value) {
                    setField(value);
                }

                public quickfix.field.UndlyInstrumentPartySubID get(
                    quickfix.field.UndlyInstrumentPartySubID value)
                    throws FieldNotFound {
                    getField(value);

                    return value;
                }

                public quickfix.field.UndlyInstrumentPartySubID getUndlyInstrumentPartySubID()
                    throws FieldNotFound {
                    quickfix.field.UndlyInstrumentPartySubID value = new quickfix.field.UndlyInstrumentPartySubID();
                    getField(value);

                    return value;
                }

                public boolean isSet(
                    quickfix.field.UndlyInstrumentPartySubID field) {
                    return isSetField(field);
                }

                public boolean isSetUndlyInstrumentPartySubID() {
                    return isSetField(1063);
                }

                public void set(
                    quickfix.field.UndlyInstrumentPartySubIDType value) {
                    setField(value);
                }

                public quickfix.field.UndlyInstrumentPartySubIDType get(
                    quickfix.field.UndlyInstrumentPartySubIDType value)
                    throws FieldNotFound {
                    getField(value);

                    return value;
                }

                public quickfix.field.UndlyInstrumentPartySubIDType getUndlyInstrumentPartySubIDType()
                    throws FieldNotFound {
                    quickfix.field.UndlyInstrumentPartySubIDType value = new quickfix.field.UndlyInstrumentPartySubIDType();
                    getField(value);

                    return value;
                }

                public boolean isSet(
                    quickfix.field.UndlyInstrumentPartySubIDType field) {
                    return isSetField(field);
                }

                public boolean isSetUndlyInstrumentPartySubIDType() {
                    return isSetField(1064);
                }
            }
        }
    }

    public static class NoLegs extends Group {
        static final long serialVersionUID = 20050617;

        public NoLegs() {
            super(555, 600,
                new int[] {
                    600, 601, 602, 603, 604, 607, 608, 609, 764, 610, 611, 248,
                    249, 250, 251, 252, 253, 257, 599, 596, 597, 598, 254, 612,
                    942, 613, 614, 615, 616, 617, 618, 619, 620, 621, 622, 623,
                    624, 556, 740, 739, 955, 956, 999, 1001, 0
                });
        }

        public void set(quickfix.fix50.component.InstrumentLeg component) {
            setComponent(component);
        }

        public quickfix.fix50.component.InstrumentLeg get(
            quickfix.fix50.component.InstrumentLeg component)
            throws FieldNotFound {
            getComponent(component);

            return component;
        }

        public quickfix.fix50.component.InstrumentLeg getInstrumentLeg()
            throws FieldNotFound {
            quickfix.fix50.component.InstrumentLeg component = new quickfix.fix50.component.InstrumentLeg();
            getComponent(component);

            return component;
        }

        public void set(quickfix.field.LegSymbol value) {
            setField(value);
        }

        public quickfix.field.LegSymbol get(quickfix.field.LegSymbol value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegSymbol getLegSymbol()
            throws FieldNotFound {
            quickfix.field.LegSymbol value = new quickfix.field.LegSymbol();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegSymbol field) {
            return isSetField(field);
        }

        public boolean isSetLegSymbol() {
            return isSetField(600);
        }

        public void set(quickfix.field.LegSymbolSfx value) {
            setField(value);
        }

        public quickfix.field.LegSymbolSfx get(
            quickfix.field.LegSymbolSfx value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegSymbolSfx getLegSymbolSfx()
            throws FieldNotFound {
            quickfix.field.LegSymbolSfx value = new quickfix.field.LegSymbolSfx();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegSymbolSfx field) {
            return isSetField(field);
        }

        public boolean isSetLegSymbolSfx() {
            return isSetField(601);
        }

        public void set(quickfix.field.LegSecurityID value) {
            setField(value);
        }

        public quickfix.field.LegSecurityID get(
            quickfix.field.LegSecurityID value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegSecurityID getLegSecurityID()
            throws FieldNotFound {
            quickfix.field.LegSecurityID value = new quickfix.field.LegSecurityID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegSecurityID field) {
            return isSetField(field);
        }

        public boolean isSetLegSecurityID() {
            return isSetField(602);
        }

        public void set(quickfix.field.LegSecurityIDSource value) {
            setField(value);
        }

        public quickfix.field.LegSecurityIDSource get(
            quickfix.field.LegSecurityIDSource value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegSecurityIDSource getLegSecurityIDSource()
            throws FieldNotFound {
            quickfix.field.LegSecurityIDSource value = new quickfix.field.LegSecurityIDSource();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegSecurityIDSource field) {
            return isSetField(field);
        }

        public boolean isSetLegSecurityIDSource() {
            return isSetField(603);
        }

        public void set(quickfix.fix50.component.LegSecAltIDGrp component) {
            setComponent(component);
        }

        public quickfix.fix50.component.LegSecAltIDGrp get(
            quickfix.fix50.component.LegSecAltIDGrp component)
            throws FieldNotFound {
            getComponent(component);

            return component;
        }

        public quickfix.fix50.component.LegSecAltIDGrp getLegSecAltIDGrp()
            throws FieldNotFound {
            quickfix.fix50.component.LegSecAltIDGrp component = new quickfix.fix50.component.LegSecAltIDGrp();
            getComponent(component);

            return component;
        }

        public void set(quickfix.field.NoLegSecurityAltID value) {
            setField(value);
        }

        public quickfix.field.NoLegSecurityAltID get(
            quickfix.field.NoLegSecurityAltID value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.NoLegSecurityAltID getNoLegSecurityAltID()
            throws FieldNotFound {
            quickfix.field.NoLegSecurityAltID value = new quickfix.field.NoLegSecurityAltID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.NoLegSecurityAltID field) {
            return isSetField(field);
        }

        public boolean isSetNoLegSecurityAltID() {
            return isSetField(604);
        }

        public void set(quickfix.field.LegProduct value) {
            setField(value);
        }

        public quickfix.field.LegProduct get(quickfix.field.LegProduct value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegProduct getLegProduct()
            throws FieldNotFound {
            quickfix.field.LegProduct value = new quickfix.field.LegProduct();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegProduct field) {
            return isSetField(field);
        }

        public boolean isSetLegProduct() {
            return isSetField(607);
        }

        public void set(quickfix.field.LegCFICode value) {
            setField(value);
        }

        public quickfix.field.LegCFICode get(quickfix.field.LegCFICode value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegCFICode getLegCFICode()
            throws FieldNotFound {
            quickfix.field.LegCFICode value = new quickfix.field.LegCFICode();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegCFICode field) {
            return isSetField(field);
        }

        public boolean isSetLegCFICode() {
            return isSetField(608);
        }

        public void set(quickfix.field.LegSecurityType value) {
            setField(value);
        }

        public quickfix.field.LegSecurityType get(
            quickfix.field.LegSecurityType value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegSecurityType getLegSecurityType()
            throws FieldNotFound {
            quickfix.field.LegSecurityType value = new quickfix.field.LegSecurityType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegSecurityType field) {
            return isSetField(field);
        }

        public boolean isSetLegSecurityType() {
            return isSetField(609);
        }

        public void set(quickfix.field.LegSecuritySubType value) {
            setField(value);
        }

        public quickfix.field.LegSecuritySubType get(
            quickfix.field.LegSecuritySubType value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegSecuritySubType getLegSecuritySubType()
            throws FieldNotFound {
            quickfix.field.LegSecuritySubType value = new quickfix.field.LegSecuritySubType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegSecuritySubType field) {
            return isSetField(field);
        }

        public boolean isSetLegSecuritySubType() {
            return isSetField(764);
        }

        public void set(quickfix.field.LegMaturityMonthYear value) {
            setField(value);
        }

        public quickfix.field.LegMaturityMonthYear get(
            quickfix.field.LegMaturityMonthYear value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegMaturityMonthYear getLegMaturityMonthYear()
            throws FieldNotFound {
            quickfix.field.LegMaturityMonthYear value = new quickfix.field.LegMaturityMonthYear();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegMaturityMonthYear field) {
            return isSetField(field);
        }

        public boolean isSetLegMaturityMonthYear() {
            return isSetField(610);
        }

        public void set(quickfix.field.LegMaturityDate value) {
            setField(value);
        }

        public quickfix.field.LegMaturityDate get(
            quickfix.field.LegMaturityDate value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegMaturityDate getLegMaturityDate()
            throws FieldNotFound {
            quickfix.field.LegMaturityDate value = new quickfix.field.LegMaturityDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegMaturityDate field) {
            return isSetField(field);
        }

        public boolean isSetLegMaturityDate() {
            return isSetField(611);
        }

        public void set(quickfix.field.LegCouponPaymentDate value) {
            setField(value);
        }

        public quickfix.field.LegCouponPaymentDate get(
            quickfix.field.LegCouponPaymentDate value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegCouponPaymentDate getLegCouponPaymentDate()
            throws FieldNotFound {
            quickfix.field.LegCouponPaymentDate value = new quickfix.field.LegCouponPaymentDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegCouponPaymentDate field) {
            return isSetField(field);
        }

        public boolean isSetLegCouponPaymentDate() {
            return isSetField(248);
        }

        public void set(quickfix.field.LegIssueDate value) {
            setField(value);
        }

        public quickfix.field.LegIssueDate get(
            quickfix.field.LegIssueDate value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegIssueDate getLegIssueDate()
            throws FieldNotFound {
            quickfix.field.LegIssueDate value = new quickfix.field.LegIssueDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegIssueDate field) {
            return isSetField(field);
        }

        public boolean isSetLegIssueDate() {
            return isSetField(249);
        }

        public void set(quickfix.field.LegRepoCollateralSecurityType value) {
            setField(value);
        }

        public quickfix.field.LegRepoCollateralSecurityType get(
            quickfix.field.LegRepoCollateralSecurityType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegRepoCollateralSecurityType getLegRepoCollateralSecurityType()
            throws FieldNotFound {
            quickfix.field.LegRepoCollateralSecurityType value = new quickfix.field.LegRepoCollateralSecurityType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegRepoCollateralSecurityType field) {
            return isSetField(field);
        }

        public boolean isSetLegRepoCollateralSecurityType() {
            return isSetField(250);
        }

        public void set(quickfix.field.LegRepurchaseTerm value) {
            setField(value);
        }

        public quickfix.field.LegRepurchaseTerm get(
            quickfix.field.LegRepurchaseTerm value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegRepurchaseTerm getLegRepurchaseTerm()
            throws FieldNotFound {
            quickfix.field.LegRepurchaseTerm value = new quickfix.field.LegRepurchaseTerm();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegRepurchaseTerm field) {
            return isSetField(field);
        }

        public boolean isSetLegRepurchaseTerm() {
            return isSetField(251);
        }

        public void set(quickfix.field.LegRepurchaseRate value) {
            setField(value);
        }

        public quickfix.field.LegRepurchaseRate get(
            quickfix.field.LegRepurchaseRate value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegRepurchaseRate getLegRepurchaseRate()
            throws FieldNotFound {
            quickfix.field.LegRepurchaseRate value = new quickfix.field.LegRepurchaseRate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegRepurchaseRate field) {
            return isSetField(field);
        }

        public boolean isSetLegRepurchaseRate() {
            return isSetField(252);
        }

        public void set(quickfix.field.LegFactor value) {
            setField(value);
        }

        public quickfix.field.LegFactor get(quickfix.field.LegFactor value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegFactor getLegFactor()
            throws FieldNotFound {
            quickfix.field.LegFactor value = new quickfix.field.LegFactor();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegFactor field) {
            return isSetField(field);
        }

        public boolean isSetLegFactor() {
            return isSetField(253);
        }

        public void set(quickfix.field.LegCreditRating value) {
            setField(value);
        }

        public quickfix.field.LegCreditRating get(
            quickfix.field.LegCreditRating value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegCreditRating getLegCreditRating()
            throws FieldNotFound {
            quickfix.field.LegCreditRating value = new quickfix.field.LegCreditRating();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegCreditRating field) {
            return isSetField(field);
        }

        public boolean isSetLegCreditRating() {
            return isSetField(257);
        }

        public void set(quickfix.field.LegInstrRegistry value) {
            setField(value);
        }

        public quickfix.field.LegInstrRegistry get(
            quickfix.field.LegInstrRegistry value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegInstrRegistry getLegInstrRegistry()
            throws FieldNotFound {
            quickfix.field.LegInstrRegistry value = new quickfix.field.LegInstrRegistry();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegInstrRegistry field) {
            return isSetField(field);
        }

        public boolean isSetLegInstrRegistry() {
            return isSetField(599);
        }

        public void set(quickfix.field.LegCountryOfIssue value) {
            setField(value);
        }

        public quickfix.field.LegCountryOfIssue get(
            quickfix.field.LegCountryOfIssue value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegCountryOfIssue getLegCountryOfIssue()
            throws FieldNotFound {
            quickfix.field.LegCountryOfIssue value = new quickfix.field.LegCountryOfIssue();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegCountryOfIssue field) {
            return isSetField(field);
        }

        public boolean isSetLegCountryOfIssue() {
            return isSetField(596);
        }

        public void set(quickfix.field.LegStateOrProvinceOfIssue value) {
            setField(value);
        }

        public quickfix.field.LegStateOrProvinceOfIssue get(
            quickfix.field.LegStateOrProvinceOfIssue value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegStateOrProvinceOfIssue getLegStateOrProvinceOfIssue()
            throws FieldNotFound {
            quickfix.field.LegStateOrProvinceOfIssue value = new quickfix.field.LegStateOrProvinceOfIssue();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegStateOrProvinceOfIssue field) {
            return isSetField(field);
        }

        public boolean isSetLegStateOrProvinceOfIssue() {
            return isSetField(597);
        }

        public void set(quickfix.field.LegLocaleOfIssue value) {
            setField(value);
        }

        public quickfix.field.LegLocaleOfIssue get(
            quickfix.field.LegLocaleOfIssue value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegLocaleOfIssue getLegLocaleOfIssue()
            throws FieldNotFound {
            quickfix.field.LegLocaleOfIssue value = new quickfix.field.LegLocaleOfIssue();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegLocaleOfIssue field) {
            return isSetField(field);
        }

        public boolean isSetLegLocaleOfIssue() {
            return isSetField(598);
        }

        public void set(quickfix.field.LegRedemptionDate value) {
            setField(value);
        }

        public quickfix.field.LegRedemptionDate get(
            quickfix.field.LegRedemptionDate value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegRedemptionDate getLegRedemptionDate()
            throws FieldNotFound {
            quickfix.field.LegRedemptionDate value = new quickfix.field.LegRedemptionDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegRedemptionDate field) {
            return isSetField(field);
        }

        public boolean isSetLegRedemptionDate() {
            return isSetField(254);
        }

        public void set(quickfix.field.LegStrikePrice value) {
            setField(value);
        }

        public quickfix.field.LegStrikePrice get(
            quickfix.field.LegStrikePrice value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegStrikePrice getLegStrikePrice()
            throws FieldNotFound {
            quickfix.field.LegStrikePrice value = new quickfix.field.LegStrikePrice();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegStrikePrice field) {
            return isSetField(field);
        }

        public boolean isSetLegStrikePrice() {
            return isSetField(612);
        }

        public void set(quickfix.field.LegStrikeCurrency value) {
            setField(value);
        }

        public quickfix.field.LegStrikeCurrency get(
            quickfix.field.LegStrikeCurrency value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegStrikeCurrency getLegStrikeCurrency()
            throws FieldNotFound {
            quickfix.field.LegStrikeCurrency value = new quickfix.field.LegStrikeCurrency();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegStrikeCurrency field) {
            return isSetField(field);
        }

        public boolean isSetLegStrikeCurrency() {
            return isSetField(942);
        }

        public void set(quickfix.field.LegOptAttribute value) {
            setField(value);
        }

        public quickfix.field.LegOptAttribute get(
            quickfix.field.LegOptAttribute value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegOptAttribute getLegOptAttribute()
            throws FieldNotFound {
            quickfix.field.LegOptAttribute value = new quickfix.field.LegOptAttribute();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegOptAttribute field) {
            return isSetField(field);
        }

        public boolean isSetLegOptAttribute() {
            return isSetField(613);
        }

        public void set(quickfix.field.LegContractMultiplier value) {
            setField(value);
        }

        public quickfix.field.LegContractMultiplier get(
            quickfix.field.LegContractMultiplier value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegContractMultiplier getLegContractMultiplier()
            throws FieldNotFound {
            quickfix.field.LegContractMultiplier value = new quickfix.field.LegContractMultiplier();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegContractMultiplier field) {
            return isSetField(field);
        }

        public boolean isSetLegContractMultiplier() {
            return isSetField(614);
        }

        public void set(quickfix.field.LegCouponRate value) {
            setField(value);
        }

        public quickfix.field.LegCouponRate get(
            quickfix.field.LegCouponRate value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegCouponRate getLegCouponRate()
            throws FieldNotFound {
            quickfix.field.LegCouponRate value = new quickfix.field.LegCouponRate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegCouponRate field) {
            return isSetField(field);
        }

        public boolean isSetLegCouponRate() {
            return isSetField(615);
        }

        public void set(quickfix.field.LegSecurityExchange value) {
            setField(value);
        }

        public quickfix.field.LegSecurityExchange get(
            quickfix.field.LegSecurityExchange value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegSecurityExchange getLegSecurityExchange()
            throws FieldNotFound {
            quickfix.field.LegSecurityExchange value = new quickfix.field.LegSecurityExchange();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegSecurityExchange field) {
            return isSetField(field);
        }

        public boolean isSetLegSecurityExchange() {
            return isSetField(616);
        }

        public void set(quickfix.field.LegIssuer value) {
            setField(value);
        }

        public quickfix.field.LegIssuer get(quickfix.field.LegIssuer value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegIssuer getLegIssuer()
            throws FieldNotFound {
            quickfix.field.LegIssuer value = new quickfix.field.LegIssuer();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegIssuer field) {
            return isSetField(field);
        }

        public boolean isSetLegIssuer() {
            return isSetField(617);
        }

        public void set(quickfix.field.EncodedLegIssuerLen value) {
            setField(value);
        }

        public quickfix.field.EncodedLegIssuerLen get(
            quickfix.field.EncodedLegIssuerLen value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EncodedLegIssuerLen getEncodedLegIssuerLen()
            throws FieldNotFound {
            quickfix.field.EncodedLegIssuerLen value = new quickfix.field.EncodedLegIssuerLen();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EncodedLegIssuerLen field) {
            return isSetField(field);
        }

        public boolean isSetEncodedLegIssuerLen() {
            return isSetField(618);
        }

        public void set(quickfix.field.EncodedLegIssuer value) {
            setField(value);
        }

        public quickfix.field.EncodedLegIssuer get(
            quickfix.field.EncodedLegIssuer value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EncodedLegIssuer getEncodedLegIssuer()
            throws FieldNotFound {
            quickfix.field.EncodedLegIssuer value = new quickfix.field.EncodedLegIssuer();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EncodedLegIssuer field) {
            return isSetField(field);
        }

        public boolean isSetEncodedLegIssuer() {
            return isSetField(619);
        }

        public void set(quickfix.field.LegSecurityDesc value) {
            setField(value);
        }

        public quickfix.field.LegSecurityDesc get(
            quickfix.field.LegSecurityDesc value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegSecurityDesc getLegSecurityDesc()
            throws FieldNotFound {
            quickfix.field.LegSecurityDesc value = new quickfix.field.LegSecurityDesc();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegSecurityDesc field) {
            return isSetField(field);
        }

        public boolean isSetLegSecurityDesc() {
            return isSetField(620);
        }

        public void set(quickfix.field.EncodedLegSecurityDescLen value) {
            setField(value);
        }

        public quickfix.field.EncodedLegSecurityDescLen get(
            quickfix.field.EncodedLegSecurityDescLen value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EncodedLegSecurityDescLen getEncodedLegSecurityDescLen()
            throws FieldNotFound {
            quickfix.field.EncodedLegSecurityDescLen value = new quickfix.field.EncodedLegSecurityDescLen();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EncodedLegSecurityDescLen field) {
            return isSetField(field);
        }

        public boolean isSetEncodedLegSecurityDescLen() {
            return isSetField(621);
        }

        public void set(quickfix.field.EncodedLegSecurityDesc value) {
            setField(value);
        }

        public quickfix.field.EncodedLegSecurityDesc get(
            quickfix.field.EncodedLegSecurityDesc value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EncodedLegSecurityDesc getEncodedLegSecurityDesc()
            throws FieldNotFound {
            quickfix.field.EncodedLegSecurityDesc value = new quickfix.field.EncodedLegSecurityDesc();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EncodedLegSecurityDesc field) {
            return isSetField(field);
        }

        public boolean isSetEncodedLegSecurityDesc() {
            return isSetField(622);
        }

        public void set(quickfix.field.LegRatioQty value) {
            setField(value);
        }

        public quickfix.field.LegRatioQty get(quickfix.field.LegRatioQty value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegRatioQty getLegRatioQty()
            throws FieldNotFound {
            quickfix.field.LegRatioQty value = new quickfix.field.LegRatioQty();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegRatioQty field) {
            return isSetField(field);
        }

        public boolean isSetLegRatioQty() {
            return isSetField(623);
        }

        public void set(quickfix.field.LegSide value) {
            setField(value);
        }

        public quickfix.field.LegSide get(quickfix.field.LegSide value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegSide getLegSide() throws FieldNotFound {
            quickfix.field.LegSide value = new quickfix.field.LegSide();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegSide field) {
            return isSetField(field);
        }

        public boolean isSetLegSide() {
            return isSetField(624);
        }

        public void set(quickfix.field.LegCurrency value) {
            setField(value);
        }

        public quickfix.field.LegCurrency get(quickfix.field.LegCurrency value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegCurrency getLegCurrency()
            throws FieldNotFound {
            quickfix.field.LegCurrency value = new quickfix.field.LegCurrency();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegCurrency field) {
            return isSetField(field);
        }

        public boolean isSetLegCurrency() {
            return isSetField(556);
        }

        public void set(quickfix.field.LegPool value) {
            setField(value);
        }

        public quickfix.field.LegPool get(quickfix.field.LegPool value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegPool getLegPool() throws FieldNotFound {
            quickfix.field.LegPool value = new quickfix.field.LegPool();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegPool field) {
            return isSetField(field);
        }

        public boolean isSetLegPool() {
            return isSetField(740);
        }

        public void set(quickfix.field.LegDatedDate value) {
            setField(value);
        }

        public quickfix.field.LegDatedDate get(
            quickfix.field.LegDatedDate value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegDatedDate getLegDatedDate()
            throws FieldNotFound {
            quickfix.field.LegDatedDate value = new quickfix.field.LegDatedDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegDatedDate field) {
            return isSetField(field);
        }

        public boolean isSetLegDatedDate() {
            return isSetField(739);
        }

        public void set(quickfix.field.LegContractSettlMonth value) {
            setField(value);
        }

        public quickfix.field.LegContractSettlMonth get(
            quickfix.field.LegContractSettlMonth value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegContractSettlMonth getLegContractSettlMonth()
            throws FieldNotFound {
            quickfix.field.LegContractSettlMonth value = new quickfix.field.LegContractSettlMonth();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegContractSettlMonth field) {
            return isSetField(field);
        }

        public boolean isSetLegContractSettlMonth() {
            return isSetField(955);
        }

        public void set(quickfix.field.LegInterestAccrualDate value) {
            setField(value);
        }

        public quickfix.field.LegInterestAccrualDate get(
            quickfix.field.LegInterestAccrualDate value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegInterestAccrualDate getLegInterestAccrualDate()
            throws FieldNotFound {
            quickfix.field.LegInterestAccrualDate value = new quickfix.field.LegInterestAccrualDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegInterestAccrualDate field) {
            return isSetField(field);
        }

        public boolean isSetLegInterestAccrualDate() {
            return isSetField(956);
        }

        public void set(quickfix.field.LegUnitofMeasure value) {
            setField(value);
        }

        public quickfix.field.LegUnitofMeasure get(
            quickfix.field.LegUnitofMeasure value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegUnitofMeasure getLegUnitofMeasure()
            throws FieldNotFound {
            quickfix.field.LegUnitofMeasure value = new quickfix.field.LegUnitofMeasure();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegUnitofMeasure field) {
            return isSetField(field);
        }

        public boolean isSetLegUnitofMeasure() {
            return isSetField(999);
        }

        public void set(quickfix.field.LegTimeUnit value) {
            setField(value);
        }

        public quickfix.field.LegTimeUnit get(quickfix.field.LegTimeUnit value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LegTimeUnit getLegTimeUnit()
            throws FieldNotFound {
            quickfix.field.LegTimeUnit value = new quickfix.field.LegTimeUnit();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LegTimeUnit field) {
            return isSetField(field);
        }

        public boolean isSetLegTimeUnit() {
            return isSetField(1001);
        }
    }

    public static class NoMDEntries extends Group {
        static final long serialVersionUID = 20050617;

        public NoMDEntries() {
            super(268, 269,
                new int[] {
                    269, 270, 15, 271, 272, 273, 274, 275, 336, 625, 276, 277,
                    282, 283, 284, 286, 59, 432, 126, 110, 18, 287, 37, 299, 288,
                    289, 346, 290, 546, 811, 58, 354, 355, 1023, 528, 1024, 332,
                    333, 1020, 63, 64, 1070, 83, 1048, 1026, 1027, 278, 453, 198,
                    40, 0
                });
        }

        public void set(quickfix.field.MDEntryType value) {
            setField(value);
        }

        public quickfix.field.MDEntryType get(quickfix.field.MDEntryType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDEntryType getMDEntryType()
            throws FieldNotFound {
            quickfix.field.MDEntryType value = new quickfix.field.MDEntryType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDEntryType field) {
            return isSetField(field);
        }

        public boolean isSetMDEntryType() {
            return isSetField(269);
        }

        public void set(quickfix.field.MDEntryPx value) {
            setField(value);
        }

        public quickfix.field.MDEntryPx get(quickfix.field.MDEntryPx value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDEntryPx getMDEntryPx()
            throws FieldNotFound {
            quickfix.field.MDEntryPx value = new quickfix.field.MDEntryPx();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDEntryPx field) {
            return isSetField(field);
        }

        public boolean isSetMDEntryPx() {
            return isSetField(270);
        }

        public void set(quickfix.field.Currency value) {
            setField(value);
        }

        public quickfix.field.Currency get(quickfix.field.Currency value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.Currency getCurrency() throws FieldNotFound {
            quickfix.field.Currency value = new quickfix.field.Currency();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.Currency field) {
            return isSetField(field);
        }

        public boolean isSetCurrency() {
            return isSetField(15);
        }

        public void set(quickfix.field.MDEntrySize value) {
            setField(value);
        }

        public quickfix.field.MDEntrySize get(quickfix.field.MDEntrySize value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDEntrySize getMDEntrySize()
            throws FieldNotFound {
            quickfix.field.MDEntrySize value = new quickfix.field.MDEntrySize();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDEntrySize field) {
            return isSetField(field);
        }

        public boolean isSetMDEntrySize() {
            return isSetField(271);
        }

        public void set(quickfix.field.MDEntryDate value) {
            setField(value);
        }

        public quickfix.field.MDEntryDate get(quickfix.field.MDEntryDate value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDEntryDate getMDEntryDate()
            throws FieldNotFound {
            quickfix.field.MDEntryDate value = new quickfix.field.MDEntryDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDEntryDate field) {
            return isSetField(field);
        }

        public boolean isSetMDEntryDate() {
            return isSetField(272);
        }

        public void set(quickfix.field.MDEntryTime value) {
            setField(value);
        }

        public quickfix.field.MDEntryTime get(quickfix.field.MDEntryTime value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDEntryTime getMDEntryTime()
            throws FieldNotFound {
            quickfix.field.MDEntryTime value = new quickfix.field.MDEntryTime();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDEntryTime field) {
            return isSetField(field);
        }

        public boolean isSetMDEntryTime() {
            return isSetField(273);
        }

        public void set(quickfix.field.TickDirection value) {
            setField(value);
        }

        public quickfix.field.TickDirection get(
            quickfix.field.TickDirection value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.TickDirection getTickDirection()
            throws FieldNotFound {
            quickfix.field.TickDirection value = new quickfix.field.TickDirection();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.TickDirection field) {
            return isSetField(field);
        }

        public boolean isSetTickDirection() {
            return isSetField(274);
        }

        public void set(quickfix.field.MDMkt value) {
            setField(value);
        }

        public quickfix.field.MDMkt get(quickfix.field.MDMkt value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDMkt getMDMkt() throws FieldNotFound {
            quickfix.field.MDMkt value = new quickfix.field.MDMkt();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDMkt field) {
            return isSetField(field);
        }

        public boolean isSetMDMkt() {
            return isSetField(275);
        }

        public void set(quickfix.field.TradingSessionID value) {
            setField(value);
        }

        public quickfix.field.TradingSessionID get(
            quickfix.field.TradingSessionID value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.TradingSessionID getTradingSessionID()
            throws FieldNotFound {
            quickfix.field.TradingSessionID value = new quickfix.field.TradingSessionID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.TradingSessionID field) {
            return isSetField(field);
        }

        public boolean isSetTradingSessionID() {
            return isSetField(336);
        }

        public void set(quickfix.field.TradingSessionSubID value) {
            setField(value);
        }

        public quickfix.field.TradingSessionSubID get(
            quickfix.field.TradingSessionSubID value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.TradingSessionSubID getTradingSessionSubID()
            throws FieldNotFound {
            quickfix.field.TradingSessionSubID value = new quickfix.field.TradingSessionSubID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.TradingSessionSubID field) {
            return isSetField(field);
        }

        public boolean isSetTradingSessionSubID() {
            return isSetField(625);
        }

        public void set(quickfix.field.QuoteCondition value) {
            setField(value);
        }

        public quickfix.field.QuoteCondition get(
            quickfix.field.QuoteCondition value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.QuoteCondition getQuoteCondition()
            throws FieldNotFound {
            quickfix.field.QuoteCondition value = new quickfix.field.QuoteCondition();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.QuoteCondition field) {
            return isSetField(field);
        }

        public boolean isSetQuoteCondition() {
            return isSetField(276);
        }

        public void set(quickfix.field.TradeCondition value) {
            setField(value);
        }

        public quickfix.field.TradeCondition get(
            quickfix.field.TradeCondition value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.TradeCondition getTradeCondition()
            throws FieldNotFound {
            quickfix.field.TradeCondition value = new quickfix.field.TradeCondition();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.TradeCondition field) {
            return isSetField(field);
        }

        public boolean isSetTradeCondition() {
            return isSetField(277);
        }

        public void set(quickfix.field.MDEntryOriginator value) {
            setField(value);
        }

        public quickfix.field.MDEntryOriginator get(
            quickfix.field.MDEntryOriginator value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDEntryOriginator getMDEntryOriginator()
            throws FieldNotFound {
            quickfix.field.MDEntryOriginator value = new quickfix.field.MDEntryOriginator();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDEntryOriginator field) {
            return isSetField(field);
        }

        public boolean isSetMDEntryOriginator() {
            return isSetField(282);
        }

        public void set(quickfix.field.LocationID value) {
            setField(value);
        }

        public quickfix.field.LocationID get(quickfix.field.LocationID value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LocationID getLocationID()
            throws FieldNotFound {
            quickfix.field.LocationID value = new quickfix.field.LocationID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LocationID field) {
            return isSetField(field);
        }

        public boolean isSetLocationID() {
            return isSetField(283);
        }

        public void set(quickfix.field.DeskID value) {
            setField(value);
        }

        public quickfix.field.DeskID get(quickfix.field.DeskID value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.DeskID getDeskID() throws FieldNotFound {
            quickfix.field.DeskID value = new quickfix.field.DeskID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.DeskID field) {
            return isSetField(field);
        }

        public boolean isSetDeskID() {
            return isSetField(284);
        }

        public void set(quickfix.field.OpenCloseSettlFlag value) {
            setField(value);
        }

        public quickfix.field.OpenCloseSettlFlag get(
            quickfix.field.OpenCloseSettlFlag value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.OpenCloseSettlFlag getOpenCloseSettlFlag()
            throws FieldNotFound {
            quickfix.field.OpenCloseSettlFlag value = new quickfix.field.OpenCloseSettlFlag();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.OpenCloseSettlFlag field) {
            return isSetField(field);
        }

        public boolean isSetOpenCloseSettlFlag() {
            return isSetField(286);
        }

        public void set(quickfix.field.TimeInForce value) {
            setField(value);
        }

        public quickfix.field.TimeInForce get(quickfix.field.TimeInForce value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.TimeInForce getTimeInForce()
            throws FieldNotFound {
            quickfix.field.TimeInForce value = new quickfix.field.TimeInForce();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.TimeInForce field) {
            return isSetField(field);
        }

        public boolean isSetTimeInForce() {
            return isSetField(59);
        }

        public void set(quickfix.field.ExpireDate value) {
            setField(value);
        }

        public quickfix.field.ExpireDate get(quickfix.field.ExpireDate value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.ExpireDate getExpireDate()
            throws FieldNotFound {
            quickfix.field.ExpireDate value = new quickfix.field.ExpireDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.ExpireDate field) {
            return isSetField(field);
        }

        public boolean isSetExpireDate() {
            return isSetField(432);
        }

        public void set(quickfix.field.ExpireTime value) {
            setField(value);
        }

        public quickfix.field.ExpireTime get(quickfix.field.ExpireTime value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.ExpireTime getExpireTime()
            throws FieldNotFound {
            quickfix.field.ExpireTime value = new quickfix.field.ExpireTime();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.ExpireTime field) {
            return isSetField(field);
        }

        public boolean isSetExpireTime() {
            return isSetField(126);
        }

        public void set(quickfix.field.MinQty value) {
            setField(value);
        }

        public quickfix.field.MinQty get(quickfix.field.MinQty value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MinQty getMinQty() throws FieldNotFound {
            quickfix.field.MinQty value = new quickfix.field.MinQty();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MinQty field) {
            return isSetField(field);
        }

        public boolean isSetMinQty() {
            return isSetField(110);
        }

        public void set(quickfix.field.ExecInst value) {
            setField(value);
        }

        public quickfix.field.ExecInst get(quickfix.field.ExecInst value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.ExecInst getExecInst() throws FieldNotFound {
            quickfix.field.ExecInst value = new quickfix.field.ExecInst();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.ExecInst field) {
            return isSetField(field);
        }

        public boolean isSetExecInst() {
            return isSetField(18);
        }

        public void set(quickfix.field.SellerDays value) {
            setField(value);
        }

        public quickfix.field.SellerDays get(quickfix.field.SellerDays value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.SellerDays getSellerDays()
            throws FieldNotFound {
            quickfix.field.SellerDays value = new quickfix.field.SellerDays();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.SellerDays field) {
            return isSetField(field);
        }

        public boolean isSetSellerDays() {
            return isSetField(287);
        }

        public void set(quickfix.field.OrderID value) {
            setField(value);
        }

        public quickfix.field.OrderID get(quickfix.field.OrderID value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.OrderID getOrderID() throws FieldNotFound {
            quickfix.field.OrderID value = new quickfix.field.OrderID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.OrderID field) {
            return isSetField(field);
        }

        public boolean isSetOrderID() {
            return isSetField(37);
        }

        public void set(quickfix.field.QuoteEntryID value) {
            setField(value);
        }

        public quickfix.field.QuoteEntryID get(
            quickfix.field.QuoteEntryID value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.QuoteEntryID getQuoteEntryID()
            throws FieldNotFound {
            quickfix.field.QuoteEntryID value = new quickfix.field.QuoteEntryID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.QuoteEntryID field) {
            return isSetField(field);
        }

        public boolean isSetQuoteEntryID() {
            return isSetField(299);
        }

        public void set(quickfix.field.MDEntryBuyer value) {
            setField(value);
        }

        public quickfix.field.MDEntryBuyer get(
            quickfix.field.MDEntryBuyer value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDEntryBuyer getMDEntryBuyer()
            throws FieldNotFound {
            quickfix.field.MDEntryBuyer value = new quickfix.field.MDEntryBuyer();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDEntryBuyer field) {
            return isSetField(field);
        }

        public boolean isSetMDEntryBuyer() {
            return isSetField(288);
        }

        public void set(quickfix.field.MDEntrySeller value) {
            setField(value);
        }

        public quickfix.field.MDEntrySeller get(
            quickfix.field.MDEntrySeller value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDEntrySeller getMDEntrySeller()
            throws FieldNotFound {
            quickfix.field.MDEntrySeller value = new quickfix.field.MDEntrySeller();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDEntrySeller field) {
            return isSetField(field);
        }

        public boolean isSetMDEntrySeller() {
            return isSetField(289);
        }

        public void set(quickfix.field.NumberOfOrders value) {
            setField(value);
        }

        public quickfix.field.NumberOfOrders get(
            quickfix.field.NumberOfOrders value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.NumberOfOrders getNumberOfOrders()
            throws FieldNotFound {
            quickfix.field.NumberOfOrders value = new quickfix.field.NumberOfOrders();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.NumberOfOrders field) {
            return isSetField(field);
        }

        public boolean isSetNumberOfOrders() {
            return isSetField(346);
        }

        public void set(quickfix.field.MDEntryPositionNo value) {
            setField(value);
        }

        public quickfix.field.MDEntryPositionNo get(
            quickfix.field.MDEntryPositionNo value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDEntryPositionNo getMDEntryPositionNo()
            throws FieldNotFound {
            quickfix.field.MDEntryPositionNo value = new quickfix.field.MDEntryPositionNo();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDEntryPositionNo field) {
            return isSetField(field);
        }

        public boolean isSetMDEntryPositionNo() {
            return isSetField(290);
        }

        public void set(quickfix.field.Scope value) {
            setField(value);
        }

        public quickfix.field.Scope get(quickfix.field.Scope value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.Scope getScope() throws FieldNotFound {
            quickfix.field.Scope value = new quickfix.field.Scope();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.Scope field) {
            return isSetField(field);
        }

        public boolean isSetScope() {
            return isSetField(546);
        }

        public void set(quickfix.field.PriceDelta value) {
            setField(value);
        }

        public quickfix.field.PriceDelta get(quickfix.field.PriceDelta value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.PriceDelta getPriceDelta()
            throws FieldNotFound {
            quickfix.field.PriceDelta value = new quickfix.field.PriceDelta();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.PriceDelta field) {
            return isSetField(field);
        }

        public boolean isSetPriceDelta() {
            return isSetField(811);
        }

        public void set(quickfix.field.Text value) {
            setField(value);
        }

        public quickfix.field.Text get(quickfix.field.Text value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.Text getText() throws FieldNotFound {
            quickfix.field.Text value = new quickfix.field.Text();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.Text field) {
            return isSetField(field);
        }

        public boolean isSetText() {
            return isSetField(58);
        }

        public void set(quickfix.field.EncodedTextLen value) {
            setField(value);
        }

        public quickfix.field.EncodedTextLen get(
            quickfix.field.EncodedTextLen value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EncodedTextLen getEncodedTextLen()
            throws FieldNotFound {
            quickfix.field.EncodedTextLen value = new quickfix.field.EncodedTextLen();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EncodedTextLen field) {
            return isSetField(field);
        }

        public boolean isSetEncodedTextLen() {
            return isSetField(354);
        }

        public void set(quickfix.field.EncodedText value) {
            setField(value);
        }

        public quickfix.field.EncodedText get(quickfix.field.EncodedText value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.EncodedText getEncodedText()
            throws FieldNotFound {
            quickfix.field.EncodedText value = new quickfix.field.EncodedText();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.EncodedText field) {
            return isSetField(field);
        }

        public boolean isSetEncodedText() {
            return isSetField(355);
        }

        public void set(quickfix.field.MDPriceLevel value) {
            setField(value);
        }

        public quickfix.field.MDPriceLevel get(
            quickfix.field.MDPriceLevel value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDPriceLevel getMDPriceLevel()
            throws FieldNotFound {
            quickfix.field.MDPriceLevel value = new quickfix.field.MDPriceLevel();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDPriceLevel field) {
            return isSetField(field);
        }

        public boolean isSetMDPriceLevel() {
            return isSetField(1023);
        }

        public void set(quickfix.field.OrderCapacity value) {
            setField(value);
        }

        public quickfix.field.OrderCapacity get(
            quickfix.field.OrderCapacity value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.OrderCapacity getOrderCapacity()
            throws FieldNotFound {
            quickfix.field.OrderCapacity value = new quickfix.field.OrderCapacity();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.OrderCapacity field) {
            return isSetField(field);
        }

        public boolean isSetOrderCapacity() {
            return isSetField(528);
        }

        public void set(quickfix.field.MDOriginType value) {
            setField(value);
        }

        public quickfix.field.MDOriginType get(
            quickfix.field.MDOriginType value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDOriginType getMDOriginType()
            throws FieldNotFound {
            quickfix.field.MDOriginType value = new quickfix.field.MDOriginType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDOriginType field) {
            return isSetField(field);
        }

        public boolean isSetMDOriginType() {
            return isSetField(1024);
        }

        public void set(quickfix.field.HighPx value) {
            setField(value);
        }

        public quickfix.field.HighPx get(quickfix.field.HighPx value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.HighPx getHighPx() throws FieldNotFound {
            quickfix.field.HighPx value = new quickfix.field.HighPx();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.HighPx field) {
            return isSetField(field);
        }

        public boolean isSetHighPx() {
            return isSetField(332);
        }

        public void set(quickfix.field.LowPx value) {
            setField(value);
        }

        public quickfix.field.LowPx get(quickfix.field.LowPx value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LowPx getLowPx() throws FieldNotFound {
            quickfix.field.LowPx value = new quickfix.field.LowPx();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LowPx field) {
            return isSetField(field);
        }

        public boolean isSetLowPx() {
            return isSetField(333);
        }

        public void set(quickfix.field.TradeVolume value) {
            setField(value);
        }

        public quickfix.field.TradeVolume get(quickfix.field.TradeVolume value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.TradeVolume getTradeVolume()
            throws FieldNotFound {
            quickfix.field.TradeVolume value = new quickfix.field.TradeVolume();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.TradeVolume field) {
            return isSetField(field);
        }

        public boolean isSetTradeVolume() {
            return isSetField(1020);
        }

        public void set(quickfix.field.SettlType value) {
            setField(value);
        }

        public quickfix.field.SettlType get(quickfix.field.SettlType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.SettlType getSettlType()
            throws FieldNotFound {
            quickfix.field.SettlType value = new quickfix.field.SettlType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.SettlType field) {
            return isSetField(field);
        }

        public boolean isSetSettlType() {
            return isSetField(63);
        }

        public void set(quickfix.field.SettlDate value) {
            setField(value);
        }

        public quickfix.field.SettlDate get(quickfix.field.SettlDate value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.SettlDate getSettlDate()
            throws FieldNotFound {
            quickfix.field.SettlDate value = new quickfix.field.SettlDate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.SettlDate field) {
            return isSetField(field);
        }

        public boolean isSetSettlDate() {
            return isSetField(64);
        }

        public void set(quickfix.field.MDQuoteType value) {
            setField(value);
        }

        public quickfix.field.MDQuoteType get(quickfix.field.MDQuoteType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDQuoteType getMDQuoteType()
            throws FieldNotFound {
            quickfix.field.MDQuoteType value = new quickfix.field.MDQuoteType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDQuoteType field) {
            return isSetField(field);
        }

        public boolean isSetMDQuoteType() {
            return isSetField(1070);
        }

        public void set(quickfix.field.RptSeq value) {
            setField(value);
        }

        public quickfix.field.RptSeq get(quickfix.field.RptSeq value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.RptSeq getRptSeq() throws FieldNotFound {
            quickfix.field.RptSeq value = new quickfix.field.RptSeq();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.RptSeq field) {
            return isSetField(field);
        }

        public boolean isSetRptSeq() {
            return isSetField(83);
        }

        public void set(quickfix.field.DealingCapacity value) {
            setField(value);
        }

        public quickfix.field.DealingCapacity get(
            quickfix.field.DealingCapacity value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.DealingCapacity getDealingCapacity()
            throws FieldNotFound {
            quickfix.field.DealingCapacity value = new quickfix.field.DealingCapacity();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.DealingCapacity field) {
            return isSetField(field);
        }

        public boolean isSetDealingCapacity() {
            return isSetField(1048);
        }

        public void set(quickfix.field.MDEntrySpotRate value) {
            setField(value);
        }

        public quickfix.field.MDEntrySpotRate get(
            quickfix.field.MDEntrySpotRate value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDEntrySpotRate getMDEntrySpotRate()
            throws FieldNotFound {
            quickfix.field.MDEntrySpotRate value = new quickfix.field.MDEntrySpotRate();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDEntrySpotRate field) {
            return isSetField(field);
        }

        public boolean isSetMDEntrySpotRate() {
            return isSetField(1026);
        }

        public void set(quickfix.field.MDEntryForwardPoints value) {
            setField(value);
        }

        public quickfix.field.MDEntryForwardPoints get(
            quickfix.field.MDEntryForwardPoints value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDEntryForwardPoints getMDEntryForwardPoints()
            throws FieldNotFound {
            quickfix.field.MDEntryForwardPoints value = new quickfix.field.MDEntryForwardPoints();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDEntryForwardPoints field) {
            return isSetField(field);
        }

        public boolean isSetMDEntryForwardPoints() {
            return isSetField(1027);
        }

        public void set(quickfix.field.MDEntryID value) {
            setField(value);
        }

        public quickfix.field.MDEntryID get(quickfix.field.MDEntryID value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.MDEntryID getMDEntryID()
            throws FieldNotFound {
            quickfix.field.MDEntryID value = new quickfix.field.MDEntryID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.MDEntryID field) {
            return isSetField(field);
        }

        public boolean isSetMDEntryID() {
            return isSetField(278);
        }

        public void set(quickfix.fix50.component.Parties component) {
            setComponent(component);
        }

        public quickfix.fix50.component.Parties get(
            quickfix.fix50.component.Parties component)
            throws FieldNotFound {
            getComponent(component);

            return component;
        }

        public quickfix.fix50.component.Parties getParties()
            throws FieldNotFound {
            quickfix.fix50.component.Parties component = new quickfix.fix50.component.Parties();
            getComponent(component);

            return component;
        }

        public void set(quickfix.field.NoPartyIDs value) {
            setField(value);
        }

        public quickfix.field.NoPartyIDs get(quickfix.field.NoPartyIDs value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.NoPartyIDs getNoPartyIDs()
            throws FieldNotFound {
            quickfix.field.NoPartyIDs value = new quickfix.field.NoPartyIDs();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.NoPartyIDs field) {
            return isSetField(field);
        }

        public boolean isSetNoPartyIDs() {
            return isSetField(453);
        }

        public void set(quickfix.field.SecondaryOrderID value) {
            setField(value);
        }

        public quickfix.field.SecondaryOrderID get(
            quickfix.field.SecondaryOrderID value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.SecondaryOrderID getSecondaryOrderID()
            throws FieldNotFound {
            quickfix.field.SecondaryOrderID value = new quickfix.field.SecondaryOrderID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.SecondaryOrderID field) {
            return isSetField(field);
        }

        public boolean isSetSecondaryOrderID() {
            return isSetField(198);
        }

        public void set(quickfix.field.OrdType value) {
            setField(value);
        }

        public quickfix.field.OrdType get(quickfix.field.OrdType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.OrdType getOrdType() throws FieldNotFound {
            quickfix.field.OrdType value = new quickfix.field.OrdType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.OrdType field) {
            return isSetField(field);
        }

        public boolean isSetOrdType() {
            return isSetField(40);
        }

        public static class NoPartyIDs extends Group {
            static final long serialVersionUID = 20050617;

            public NoPartyIDs() {
                super(453, 448, new int[] { 448, 447, 452, 802, 0 });
            }

            public void set(quickfix.field.PartyID value) {
                setField(value);
            }

            public quickfix.field.PartyID get(quickfix.field.PartyID value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.PartyID getPartyID()
                throws FieldNotFound {
                quickfix.field.PartyID value = new quickfix.field.PartyID();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.PartyID field) {
                return isSetField(field);
            }

            public boolean isSetPartyID() {
                return isSetField(448);
            }

            public void set(quickfix.field.PartyIDSource value) {
                setField(value);
            }

            public quickfix.field.PartyIDSource get(
                quickfix.field.PartyIDSource value) throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.PartyIDSource getPartyIDSource()
                throws FieldNotFound {
                quickfix.field.PartyIDSource value = new quickfix.field.PartyIDSource();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.PartyIDSource field) {
                return isSetField(field);
            }

            public boolean isSetPartyIDSource() {
                return isSetField(447);
            }

            public void set(quickfix.field.PartyRole value) {
                setField(value);
            }

            public quickfix.field.PartyRole get(quickfix.field.PartyRole value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.PartyRole getPartyRole()
                throws FieldNotFound {
                quickfix.field.PartyRole value = new quickfix.field.PartyRole();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.PartyRole field) {
                return isSetField(field);
            }

            public boolean isSetPartyRole() {
                return isSetField(452);
            }

            public void set(quickfix.fix50.component.PtysSubGrp component) {
                setComponent(component);
            }

            public quickfix.fix50.component.PtysSubGrp get(
                quickfix.fix50.component.PtysSubGrp component)
                throws FieldNotFound {
                getComponent(component);

                return component;
            }

            public quickfix.fix50.component.PtysSubGrp getPtysSubGrp()
                throws FieldNotFound {
                quickfix.fix50.component.PtysSubGrp component = new quickfix.fix50.component.PtysSubGrp();
                getComponent(component);

                return component;
            }

            public void set(quickfix.field.NoPartySubIDs value) {
                setField(value);
            }

            public quickfix.field.NoPartySubIDs get(
                quickfix.field.NoPartySubIDs value) throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.NoPartySubIDs getNoPartySubIDs()
                throws FieldNotFound {
                quickfix.field.NoPartySubIDs value = new quickfix.field.NoPartySubIDs();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.NoPartySubIDs field) {
                return isSetField(field);
            }

            public boolean isSetNoPartySubIDs() {
                return isSetField(802);
            }

            public static class NoPartySubIDs extends Group {
                static final long serialVersionUID = 20050617;

                public NoPartySubIDs() {
                    super(802, 523, new int[] { 523, 803, 0 });
                }

                public void set(quickfix.field.PartySubID value) {
                    setField(value);
                }

                public quickfix.field.PartySubID get(
                    quickfix.field.PartySubID value) throws FieldNotFound {
                    getField(value);

                    return value;
                }

                public quickfix.field.PartySubID getPartySubID()
                    throws FieldNotFound {
                    quickfix.field.PartySubID value = new quickfix.field.PartySubID();
                    getField(value);

                    return value;
                }

                public boolean isSet(quickfix.field.PartySubID field) {
                    return isSetField(field);
                }

                public boolean isSetPartySubID() {
                    return isSetField(523);
                }

                public void set(quickfix.field.PartySubIDType value) {
                    setField(value);
                }

                public quickfix.field.PartySubIDType get(
                    quickfix.field.PartySubIDType value)
                    throws FieldNotFound {
                    getField(value);

                    return value;
                }

                public quickfix.field.PartySubIDType getPartySubIDType()
                    throws FieldNotFound {
                    quickfix.field.PartySubIDType value = new quickfix.field.PartySubIDType();
                    getField(value);

                    return value;
                }

                public boolean isSet(quickfix.field.PartySubIDType field) {
                    return isSetField(field);
                }

                public boolean isSetPartySubIDType() {
                    return isSetField(803);
                }
            }
        }
    }

    public static class NoRoutingIDs extends Group {
        static final long serialVersionUID = 20050617;

        public NoRoutingIDs() {
            super(215, 216, new int[] { 216, 217, 0 });
        }

        public void set(quickfix.field.RoutingType value) {
            setField(value);
        }

        public quickfix.field.RoutingType get(quickfix.field.RoutingType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.RoutingType getRoutingType()
            throws FieldNotFound {
            quickfix.field.RoutingType value = new quickfix.field.RoutingType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.RoutingType field) {
            return isSetField(field);
        }

        public boolean isSetRoutingType() {
            return isSetField(216);
        }

        public void set(quickfix.field.RoutingID value) {
            setField(value);
        }

        public quickfix.field.RoutingID get(quickfix.field.RoutingID value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.RoutingID getRoutingID()
            throws FieldNotFound {
            quickfix.field.RoutingID value = new quickfix.field.RoutingID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.RoutingID field) {
            return isSetField(field);
        }

        public boolean isSetRoutingID() {
            return isSetField(217);
        }
    }
}
