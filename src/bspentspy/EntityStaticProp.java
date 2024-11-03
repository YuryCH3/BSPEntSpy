package bspentspy;

import java.util.HashMap;
import java.util.Map;

public class EntityStaticProp extends Entity {
	short[] leaves;
	byte flags;
	
	public EntityStaticProp() {
		setKeyVal("classname", "prop_static");
	}
	
	public void delKeyVal(String k) {
		return;
	}

	public void delKeyValById(int uniqueId) {
		return;
	}

	public void delKeyVal(int i) {
		return;
	}
	
	public void changeKey(String from, String to) {
		return;
	}

	public void changeKey(Integer uniqueId, String to) {
		return;
	}
	
	public boolean canBeRemoved() {
		return false;
	}
	
	public boolean shouldSave() {
		return false;
	}

	// https://developer.valvesoftware.com/wiki/BSP_(Source)/Static_prop_flags
	private static HashMap<Integer, String> propStaticFlagKeywords = new HashMap<Integer, String>();
	static {
//		propStaticFlagKeywords.put(0x04, "disableflashlight");
		propStaticFlagKeywords.put(0x08, "ignorenormals");
//		propStaticFlagKeywords.put(0x10, "disableshadows");
//		propStaticFlagKeywords.put(0x20, "disableshadows");
//		propStaticFlagKeywords.put(0x20, "drawinfastreflection");
		propStaticFlagKeywords.put(0x40, "disablevertexlighting");
		propStaticFlagKeywords.put(0x80, "disableselfshadowing");
		propStaticFlagKeywords.put(0x100, "generatelightmaps");
	}

	public void parseFlagsToKeywords() {
		HashMap<String, Integer> flagsSet = new HashMap<>();
		for (Map.Entry<Integer, String> keyFlagPair : propStaticFlagKeywords.entrySet()) {
			if ((this.flags & keyFlagPair.getKey()) > 0) {
				flagsSet.put(keyFlagPair.getValue(), 1);
			}
			else {
				flagsSet.put(keyFlagPair.getValue(), 0);
			}
		}

		for (Map.Entry<String, Integer> flagValue : flagsSet.entrySet()) {
			this.setKeyVal(flagValue.getKey(), Integer.toUnsignedString(flagValue.getValue()));
		}
	}

	public void parseFlagsFromKeywords() {
		for (Map.Entry<Integer, String> keyFlagPair : propStaticFlagKeywords.entrySet()) {
			String currentTextValue = this.getKeyValue(keyFlagPair.getValue());
			if (currentTextValue == null || currentTextValue.isBlank())
				continue;

			try {
				if (Integer.parseInt(currentTextValue) == 1) {
					byte rh = ((byte) ((int)keyFlagPair.getKey()));
					this.flags |= rh;
				}
				else {
					byte rh = ((byte) ~((int)keyFlagPair.getKey()));
					this.flags &= rh;
				}
			}
			catch (Exception e) {
				System.err.println("Failed to parse value of \"" + keyFlagPair.getValue() + "\": " + e.getMessage());
			}
		}
	}
}
