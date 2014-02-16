package quickfix.fix50.component;

import quickfix.FieldNotFound;
import quickfix.Group;


public class NstdPtysSubGrp extends quickfix.MessageComponent {
    static final long serialVersionUID = 20050617;
    public static final String MSGTYPE = "";
    private int[] componentFields = {  };
    private int[] componentGroups = { 804, };

    public NstdPtysSubGrp() {
        super();
    }

    protected int[] getFields() {
        return componentFields;
    }

    protected int[] getGroupFields() {
        return componentGroups;
    }

    public void set(quickfix.field.NoNestedPartySubIDs value) {
        setField(value);
    }

    public quickfix.field.NoNestedPartySubIDs get(
        quickfix.field.NoNestedPartySubIDs value) throws FieldNotFound {
        getField(value);

        return value;
    }

    public quickfix.field.NoNestedPartySubIDs getNoNestedPartySubIDs()
        throws FieldNotFound {
        quickfix.field.NoNestedPartySubIDs value = new quickfix.field.NoNestedPartySubIDs();
        getField(value);

        return value;
    }

    public boolean isSet(quickfix.field.NoNestedPartySubIDs field) {
        return isSetField(field);
    }

    public boolean isSetNoNestedPartySubIDs() {
        return isSetField(804);
    }

    public static class NoNestedPartySubIDs extends Group {
        static final long serialVersionUID = 20050617;

        public NoNestedPartySubIDs() {
            super(804, 545, new int[] { 545, 805, 0 });
        }

        public void set(quickfix.field.NestedPartySubID value) {
            setField(value);
        }

        public quickfix.field.NestedPartySubID get(
            quickfix.field.NestedPartySubID value) throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.NestedPartySubID getNestedPartySubID()
            throws FieldNotFound {
            quickfix.field.NestedPartySubID value = new quickfix.field.NestedPartySubID();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.NestedPartySubID field) {
            return isSetField(field);
        }

        public boolean isSetNestedPartySubID() {
            return isSetField(545);
        }

        public void set(quickfix.field.NestedPartySubIDType value) {
            setField(value);
        }

        public quickfix.field.NestedPartySubIDType get(
            quickfix.field.NestedPartySubIDType value)
            throws FieldNotFound {
            getField(value);

            return value;
        }

        public quickfix.field.NestedPartySubIDType getNestedPartySubIDType()
            throws FieldNotFound {
            quickfix.field.NestedPartySubIDType value = new quickfix.field.NestedPartySubIDType();
            getField(value);

            return value;
        }

        public boolean isSet(quickfix.field.NestedPartySubIDType field) {
            return isSetField(field);
        }

        public boolean isSetNestedPartySubIDType() {
            return isSetField(805);
        }
    }
}
