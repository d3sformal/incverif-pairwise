/**
 * 
 */
package papabench.core.fbw.modules.impl;

import papabench.core.commons.conf.FBWMode;
import papabench.core.fbw.modules.FBWStatus;

/**
 * TODO comment
 * 
 * @author Michal Malohlava
 *
 */
public class FBWStatusImpl implements FBWStatus {
	
	protected FBWMode fbwMode;
	protected boolean isMega128OK = true;
	protected boolean isRadioOK = true;
	protected boolean isRadioReallyLost = false;

	public FBWMode getFBWMode() {
		return this.fbwMode;
	}

	public boolean isMega128OK() {
		return this.isMega128OK;
	}

	public boolean isRadioOK() {
		return this.isRadioOK;
	}

	public boolean isRadioReallyLost() {
		return this.isRadioReallyLost;
	}

	public void setFBWMode(FBWMode mode) {
		this.fbwMode = mode;		
	}

	public void setMega128OK(boolean value) {
		this.isMega128OK = value;
	}

	public void setRadioOK(boolean value) {
		this.isRadioOK = value;
	}

	public void setRadioReallyLost(boolean value) {
		this.isRadioReallyLost = value;
	}

}
