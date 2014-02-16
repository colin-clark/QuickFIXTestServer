package quickfix.fix50.component;

import quickfix.FieldNotFound;
import quickfix.Group;


public class PosUndInstrmtGrp extends quickfix.MessageComponent {
    static final long serialVersionUID = 20050617;
    public static final String MSGTYPE = "";
    private int[] componentFields = {  };
    private int[] componentGroups = { 711, };

    public PosUndInstrmtGrp() {
        super();
    }

    protected int[] getFields() {
        return componentFields;
    }

    protected int[] getGroupFields() {
        return componentGroups;
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

    public static class NoUnderlyings extends Group {
        static final long serialVersionUID = 20050617;

        public NoUnderlyings() {
            super(711, 311,
                new int[] {
                    311, 312, 309, 305, 457, 462, 463, 310, 763, 313, 542, 241,
                    242, 243, 244, 245, 246, 256, 595, 592, 593, 594, 247, 316,
                    941, 317, 436, 435, 308, 306, 362, 363, 307, 364, 365, 877,
                    878, 318, 879, 810, 882, 883, 884, 885, 886, 887, 972, 975,
                    973, 974, 998, 1000, 1038, 1058, 1039, 1044, 1045, 1046, 732,
                    733, 984, 1037, 0
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

        public void set(quickfix.field.UnderlyingSettlPrice value) {
            setField(value);
        }

        public quickfix.field.UnderlyingSettlPrice get(
            quickfix.field.UnderlyingSettlPrice value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingSettlPrice getUnderlyingSettlPrice()
            throws FieldNotFound {
            quickfix.field.UnderlyingSettlPrice value = new quickfix.field.UnderlyingSettlPrice();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingSettlPrice field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingSettlPrice() {
            return isSetField(732);
        }

        public void set(quickfix.field.UnderlyingSettlPriceType value) {
            setField(value);
        }

        public quickfix.field.UnderlyingSettlPriceType get(
            quickfix.field.UnderlyingSettlPriceType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingSettlPriceType getUnderlyingSettlPriceType()
            throws FieldNotFound {
            quickfix.field.UnderlyingSettlPriceType value = new quickfix.field.UnderlyingSettlPriceType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingSettlPriceType field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingSettlPriceType() {
            return isSetField(733);
        }

        public void set(quickfix.fix50.component.UnderlyingAmount component) {
            setComponent(component);
        }

        public quickfix.fix50.component.UnderlyingAmount get(
            quickfix.fix50.component.UnderlyingAmount component)
            throws FieldNotFound {
            getComponent(component);

            return component;
        }

        public quickfix.fix50.component.UnderlyingAmount getUnderlyingAmount()
            throws FieldNotFound {
            quickfix.fix50.component.UnderlyingAmount component = new quickfix.fix50.component.UnderlyingAmount();
            getComponent(component);

            return component;
        }

        public void set(quickfix.field.NoUnderlyingAmounts value) {
            setField(value);
        }

        public quickfix.field.NoUnderlyingAmounts get(
            quickfix.field.NoUnderlyingAmounts value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.NoUnderlyingAmounts getNoUnderlyingAmounts()
            throws FieldNotFound {
            quickfix.field.NoUnderlyingAmounts value = new quickfix.field.NoUnderlyingAmounts();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.NoUnderlyingAmounts field) {
            return isSetField(field);
        }

        public boolean isSetNoUnderlyingAmounts() {
            return isSetField(984);
        }

        public void set(quickfix.field.UnderlyingDeliveryAmount value) {
            setField(value);
        }

        public quickfix.field.UnderlyingDeliveryAmount get(
            quickfix.field.UnderlyingDeliveryAmount value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.UnderlyingDeliveryAmount getUnderlyingDeliveryAmount()
            throws FieldNotFound {
            quickfix.field.UnderlyingDeliveryAmount value = new quickfix.field.UnderlyingDeliveryAmount();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.UnderlyingDeliveryAmount field) {
            return isSetField(field);
        }

        public boolean isSetUnderlyingDeliveryAmount() {
            return isSetField(1037);
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

        public static class NoUnderlyingAmounts extends Group {
            static final long serialVersionUID = 20050617;

            public NoUnderlyingAmounts() {
                super(984, 985, new int[] { 985, 986, 987, 988, 0 });
            }

            public void set(quickfix.field.UnderlyingPayAmount value) {
                setField(value);
            }

            public quickfix.field.UnderlyingPayAmount get(
                quickfix.field.UnderlyingPayAmount value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.UnderlyingPayAmount getUnderlyingPayAmount()
                throws FieldNotFound {
                quickfix.field.UnderlyingPayAmount value = new quickfix.field.UnderlyingPayAmount();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.UnderlyingPayAmount field) {
                return isSetField(field);
            }

            public boolean isSetUnderlyingPayAmount() {
                return isSetField(985);
            }

            public void set(quickfix.field.UnderlyingCollectAmount value) {
                setField(value);
            }

            public quickfix.field.UnderlyingCollectAmount get(
                quickfix.field.UnderlyingCollectAmount value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.UnderlyingCollectAmount getUnderlyingCollectAmount()
                throws FieldNotFound {
                quickfix.field.UnderlyingCollectAmount value = new quickfix.field.UnderlyingCollectAmount();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.UnderlyingCollectAmount field) {
                return isSetField(field);
            }

            public boolean isSetUnderlyingCollectAmount() {
                return isSetField(986);
            }

            public void set(quickfix.field.UnderlyingSettlementDate value) {
                setField(value);
            }

            public quickfix.field.UnderlyingSettlementDate get(
                quickfix.field.UnderlyingSettlementDate value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.UnderlyingSettlementDate getUnderlyingSettlementDate()
                throws FieldNotFound {
                quickfix.field.UnderlyingSettlementDate value = new quickfix.field.UnderlyingSettlementDate();
                getField(value);

                return value;
            }

            public boolean isSet(quickfix.field.UnderlyingSettlementDate field) {
                return isSetField(field);
            }

            public boolean isSetUnderlyingSettlementDate() {
                return isSetField(987);
            }

            public void set(quickfix.field.UnderlyingSettlementStatus value) {
                setField(value);
            }

            public quickfix.field.UnderlyingSettlementStatus get(
                quickfix.field.UnderlyingSettlementStatus value)
                throws FieldNotFound {
                getField(value);

                return value;
            }

            public quickfix.field.UnderlyingSettlementStatus getUnderlyingSettlementStatus()
                throws FieldNotFound {
                quickfix.field.UnderlyingSettlementStatus value = new quickfix.field.UnderlyingSettlementStatus();
                getField(value);

                return value;
            }

            public boolean isSet(
                quickfix.field.UnderlyingSettlementStatus field) {
                return isSetField(field);
            }

            public boolean isSetUnderlyingSettlementStatus() {
                return isSetField(988);
            }
        }
    }
}
