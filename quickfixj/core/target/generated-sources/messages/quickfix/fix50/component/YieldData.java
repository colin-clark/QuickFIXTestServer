package quickfix.fix50.component;

import quickfix.FieldNotFound;


public class YieldData extends quickfix.MessageComponent {
    static final long serialVersionUID = 20050617;
    public static final String MSGTYPE = "";
    private int[] componentFields = { 235, 236, 701, 696, 697, 698, };
    private int[] componentGroups = {  };

    public YieldData() {
        super();
    }

    protected int[] getFields() {
        return componentFields;
    }

    protected int[] getGroupFields() {
        return componentGroups;
    }

    public void set(quickfix.field.YieldType value) {
        setField(value);
    }

    public quickfix.field.YieldType get(quickfix.field.YieldType value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.YieldType getYieldType() throws FieldNotFound {
        quickfix.field.YieldType value = new quickfix.field.YieldType();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.YieldType field) {
        return isSetField(field);
    }

    public boolean isSetYieldType() {
        return isSetField(235);
    }

    public void set(quickfix.field.Yield value) {
        setField(value);
    }

    public quickfix.field.Yield get(quickfix.field.Yield value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.Yield getYield() throws FieldNotFound {
        quickfix.field.Yield value = new quickfix.field.Yield();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.Yield field) {
        return isSetField(field);
    }

    public boolean isSetYield() {
        return isSetField(236);
    }

    public void set(quickfix.field.YieldCalcDate value) {
        setField(value);
    }

    public quickfix.field.YieldCalcDate get(quickfix.field.YieldCalcDate value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.YieldCalcDate getYieldCalcDate()
        throws FieldNotFound {
        quickfix.field.YieldCalcDate value = new quickfix.field.YieldCalcDate();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.YieldCalcDate field) {
        return isSetField(field);
    }

    public boolean isSetYieldCalcDate() {
        return isSetField(701);
    }

    public void set(quickfix.field.YieldRedemptionDate value) {
        setField(value);
    }

    public quickfix.field.YieldRedemptionDate get(
        quickfix.field.YieldRedemptionDate value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.YieldRedemptionDate getYieldRedemptionDate()
        throws FieldNotFound {
        quickfix.field.YieldRedemptionDate value = new quickfix.field.YieldRedemptionDate();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.YieldRedemptionDate field) {
        return isSetField(field);
    }

    public boolean isSetYieldRedemptionDate() {
        return isSetField(696);
    }

    public void set(quickfix.field.YieldRedemptionPrice value) {
        setField(value);
    }

    public quickfix.field.YieldRedemptionPrice get(
        quickfix.field.YieldRedemptionPrice value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.YieldRedemptionPrice getYieldRedemptionPrice()
        throws FieldNotFound {
        quickfix.field.YieldRedemptionPrice value = new quickfix.field.YieldRedemptionPrice();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.YieldRedemptionPrice field) {
        return isSetField(field);
    }

    public boolean isSetYieldRedemptionPrice() {
        return isSetField(697);
    }

    public void set(quickfix.field.YieldRedemptionPriceType value) {
        setField(value);
    }

    public quickfix.field.YieldRedemptionPriceType get(
        quickfix.field.YieldRedemptionPriceType value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.YieldRedemptionPriceType getYieldRedemptionPriceType()
        throws FieldNotFound {
        quickfix.field.YieldRedemptionPriceType value = new quickfix.field.YieldRedemptionPriceType();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.YieldRedemptionPriceType field) {
        return isSetField(field);
    }

    public boolean isSetYieldRedemptionPriceType() {
        return isSetField(698);
    }
}
