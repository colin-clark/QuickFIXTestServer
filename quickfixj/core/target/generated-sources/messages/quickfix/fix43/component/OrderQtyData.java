package quickfix.fix43.component;

import quickfix.FieldNotFound;


public class OrderQtyData extends quickfix.MessageComponent {
    static final long serialVersionUID = 20050617;
    public static final String MSGTYPE = "";
    private int[] componentFields = { 38, 152, 516, 468, 469, };
    private int[] componentGroups = {  };

    public OrderQtyData() {
        super();
    }

    protected int[] getFields() {
        return componentFields;
    }

    protected int[] getGroupFields() {
        return componentGroups;
    }

    public void set(quickfix.field.OrderQty value) {
        setField(value);
    }

    public quickfix.field.OrderQty get(quickfix.field.OrderQty value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.OrderQty getOrderQty() throws FieldNotFound {
        quickfix.field.OrderQty value = new quickfix.field.OrderQty();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.OrderQty field) {
        return isSetField(field);
    }

    public boolean isSetOrderQty() {
        return isSetField(38);
    }

    public void set(quickfix.field.CashOrderQty value) {
        setField(value);
    }

    public quickfix.field.CashOrderQty get(quickfix.field.CashOrderQty value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.CashOrderQty getCashOrderQty()
        throws FieldNotFound {
        quickfix.field.CashOrderQty value = new quickfix.field.CashOrderQty();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.CashOrderQty field) {
        return isSetField(field);
    }

    public boolean isSetCashOrderQty() {
        return isSetField(152);
    }

    public void set(quickfix.field.OrderPercent value) {
        setField(value);
    }

    public quickfix.field.OrderPercent get(quickfix.field.OrderPercent value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.OrderPercent getOrderPercent()
        throws FieldNotFound {
        quickfix.field.OrderPercent value = new quickfix.field.OrderPercent();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.OrderPercent field) {
        return isSetField(field);
    }

    public boolean isSetOrderPercent() {
        return isSetField(516);
    }

    public void set(quickfix.field.RoundingDirection value) {
        setField(value);
    }

    public quickfix.field.RoundingDirection get(
        quickfix.field.RoundingDirection value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.RoundingDirection getRoundingDirection()
        throws FieldNotFound {
        quickfix.field.RoundingDirection value = new quickfix.field.RoundingDirection();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.RoundingDirection field) {
        return isSetField(field);
    }

    public boolean isSetRoundingDirection() {
        return isSetField(468);
    }

    public void set(quickfix.field.RoundingModulus value) {
        setField(value);
    }

    public quickfix.field.RoundingModulus get(
        quickfix.field.RoundingModulus value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.RoundingModulus getRoundingModulus()
        throws FieldNotFound {
        quickfix.field.RoundingModulus value = new quickfix.field.RoundingModulus();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.RoundingModulus field) {
        return isSetField(field);
    }

    public boolean isSetRoundingModulus() {
        return isSetField(469);
    }
}
