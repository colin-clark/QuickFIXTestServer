package com.cep.commons;

public class EntityField {
	public String fieldName = null;
	public String fieldType = null;
	public String fieldDescription = null;
	public int fieldLength = 0;
	
	public EntityField() {
	}
	
	public EntityField(String fieldName, String fieldDescription, String fieldType, int fieldLength) {
		super();
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.fieldLength = fieldLength;
		this.fieldDescription = fieldDescription;
	}
	public String getFieldName() {
		return this.fieldName;
	}
	public String getFieldDescription() {
		return this.fieldDescription;
	}
	public String getFieldType() {
		return this.fieldType;
	}
	public int getFieldLength() {
		return this.fieldLength;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
	public void setFieldLength(int fieldLength) {
		this.fieldLength = fieldLength;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.fieldDescription == null) ? 0 : this.fieldDescription
						.hashCode());
		result = prime * result + this.fieldLength;
		result = prime * result
				+ ((this.fieldName == null) ? 0 : this.fieldName.hashCode());
		result = prime * result
				+ ((this.fieldType == null) ? 0 : this.fieldType.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityField other = (EntityField) obj;
		if (this.fieldDescription == null) {
			if (other.fieldDescription != null)
				return false;
		} else if (!this.fieldDescription.equals(other.fieldDescription))
			return false;
		if (this.fieldLength != other.fieldLength)
			return false;
		if (this.fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!this.fieldName.equals(other.fieldName))
			return false;
		if (this.fieldType == null) {
			if (other.fieldType != null)
				return false;
		} else if (!this.fieldType.equals(other.fieldType))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "EntityField [fieldDescription=" + this.fieldDescription
				+ ", fieldLength=" + this.fieldLength + ", fieldName="
				+ this.fieldName + ", fieldType=" + this.fieldType + "]";
	}

	public void setFieldDescription(String fieldDescription) {
		this.fieldDescription = fieldDescription;
	}
}
