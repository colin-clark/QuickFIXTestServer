package quickfix.fix50.component;

import quickfix.FieldNotFound;
import quickfix.Group;


public class OrdListStatGrp extends quickfix.MessageComponent {
    static final long serialVersionUID = 20050617;
    public static final String MSGTYPE = "";
    private int[] componentFields = {  };
    private int[] componentGroups = { 73, };

    public OrdListStatGrp() {
        super();
    }

    protected int[] getFields() {
        return componentFields;
    }

    protected int[] getGroupFields() {
        return componentGroups;
    }

    public void set(quickfix.field.NoOrders value) {
        setField(value);
    }

    public quickfix.field.NoOrders get(quickfix.field.NoOrders value)
        throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.NoOrders getNoOrders() throws FieldNotFound {
        quickfix.field.NoOrders value = new quickfix.field.NoOrders();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.NoOrders field) {
        return isSetField(field);
    }

    public boolean isSetNoOrders() {
        return isSetField(73);
    }

    public static class NoOrders extends Group {
        static final long serialVersionUID = 20050617;

        public NoOrders() {
            super(73, 11,
                new int[] { 11, 526, 14, 39, 636, 151, 84, 6, 103, 58, 354, 355, 0 });
        }

        public void set(quickfix.field.ClOrdID value) {
            setField(value);
        }

        public quickfix.field.ClOrdID get(quickfix.field.ClOrdID value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.ClOrdID getClOrdID() throws FieldNotFound {
            quickfix.field.ClOrdID value = new quickfix.field.ClOrdID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.ClOrdID field) {
            return isSetField(field);
        }

        public boolean isSetClOrdID() {
            return isSetField(11);
        }

        public void set(quickfix.field.SecondaryClOrdID value) {
            setField(value);
        }

        public quickfix.field.SecondaryClOrdID get(
            quickfix.field.SecondaryClOrdID value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.SecondaryClOrdID getSecondaryClOrdID()
            throws FieldNotFound {
            quickfix.field.SecondaryClOrdID value = new quickfix.field.SecondaryClOrdID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.SecondaryClOrdID field) {
            return isSetField(field);
        }

        public boolean isSetSecondaryClOrdID() {
            return isSetField(526);
        }

        public void set(quickfix.field.CumQty value) {
            setField(value);
        }

        public quickfix.field.CumQty get(quickfix.field.CumQty value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.CumQty getCumQty() throws FieldNotFound {
            quickfix.field.CumQty value = new quickfix.field.CumQty();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.CumQty field) {
            return isSetField(field);
        }

        public boolean isSetCumQty() {
            return isSetField(14);
        }

        public void set(quickfix.field.OrdStatus value) {
            setField(value);
        }

        public quickfix.field.OrdStatus get(quickfix.field.OrdStatus value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.OrdStatus getOrdStatus()
            throws FieldNotFound {
            quickfix.field.OrdStatus value = new quickfix.field.OrdStatus();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.OrdStatus field) {
            return isSetField(field);
        }

        public boolean isSetOrdStatus() {
            return isSetField(39);
        }

        public void set(quickfix.field.WorkingIndicator value) {
            setField(value);
        }

        public quickfix.field.WorkingIndicator get(
            quickfix.field.WorkingIndicator value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.WorkingIndicator getWorkingIndicator()
            throws FieldNotFound {
            quickfix.field.WorkingIndicator value = new quickfix.field.WorkingIndicator();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.WorkingIndicator field) {
            return isSetField(field);
        }

        public boolean isSetWorkingIndicator() {
            return isSetField(636);
        }

        public void set(quickfix.field.LeavesQty value) {
            setField(value);
        }

        public quickfix.field.LeavesQty get(quickfix.field.LeavesQty value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.LeavesQty getLeavesQty()
            throws FieldNotFound {
            quickfix.field.LeavesQty value = new quickfix.field.LeavesQty();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.LeavesQty field) {
            return isSetField(field);
        }

        public boolean isSetLeavesQty() {
            return isSetField(151);
        }

        public void set(quickfix.field.CxlQty value) {
            setField(value);
        }

        public quickfix.field.CxlQty get(quickfix.field.CxlQty value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.CxlQty getCxlQty() throws FieldNotFound {
            quickfix.field.CxlQty value = new quickfix.field.CxlQty();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.CxlQty field) {
            return isSetField(field);
        }

        public boolean isSetCxlQty() {
            return isSetField(84);
        }

        public void set(quickfix.field.AvgPx value) {
            setField(value);
        }

        public quickfix.field.AvgPx get(quickfix.field.AvgPx value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.AvgPx getAvgPx() throws FieldNotFound {
            quickfix.field.AvgPx value = new quickfix.field.AvgPx();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.AvgPx field) {
            return isSetField(field);
        }

        public boolean isSetAvgPx() {
            return isSetField(6);
        }

        public void set(quickfix.field.OrdRejReason value) {
            setField(value);
        }

        public quickfix.field.OrdRejReason get(
            quickfix.field.OrdRejReason value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.OrdRejReason getOrdRejReason()
            throws FieldNotFound {
            quickfix.field.OrdRejReason value = new quickfix.field.OrdRejReason();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.OrdRejReason field) {
            return isSetField(field);
        }

        public boolean isSetOrdRejReason() {
            return isSetField(103);
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
    }
}
