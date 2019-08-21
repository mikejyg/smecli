package mikejyg.socket;

/**
 * Enumerations for TlvPacket's packetType.
 */
public enum TlvPacketType {
	CONTAINER(1),
	STRING(2)
	;
	
	public static class IllegalValueException extends Exception {
		private static final long serialVersionUID = 1L;
	};
	
	private final int idValue;
	
	private TlvPacketType(int idValue) {
		this.idValue = idValue;
	}
	
	public int intValue() {
		return idValue;
	}

	public static TlvPacketType getTlvPacketType(int intValue) throws IllegalValueException {
		for ( TlvPacketType packetType : values() ) {
			if ( packetType.intValue() == intValue ) {
				return packetType;
			}
		}
		throw new IllegalValueException();
	}
	
	
}
