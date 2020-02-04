package walkGenerators.classic.alod.services.scripts.model;

public class StringString implements Comparable<StringString> {

	public String string1;
	public String string2;

	public StringString(String string1, String string2) {
		this.string1 = string1;
		this.string2 = string2;

	}

	@Override
	public boolean equals(Object that) {
		if (that instanceof StringString) {
			if (this.compareTo((StringString) that) == 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return 37 * string1.hashCode() + string2.hashCode(); 
		}
	
	@Override
	public int compareTo(StringString that) {
		if ((this.string1.equals(that.string1) && this.string2.equals(that.string2))
				|| (this.string1.equals(that.string2) && this.string2.equals(that.string1))) {
			return 0;
		} else {
			return -1;
		}
	}

}
