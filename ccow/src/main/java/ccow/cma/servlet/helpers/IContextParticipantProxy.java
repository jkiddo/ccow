package ccow.cma.servlet.helpers;

import java.net.URI;

import ccow.cma.IContextParticipant;

public interface IContextParticipantProxy extends IContextParticipant {

	long getParticipantCoupon();

	String getApplicationName();

	URI getURI();

}
