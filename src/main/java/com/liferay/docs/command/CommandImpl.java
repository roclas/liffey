package com.liferay.docs.command;

import com.liferay.osgi.shell.Interpreter;
import com.liferay.portal.service.UserLocalService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(
	immediate = true,
	property = {
	},
	service = Command.class
)
public class CommandImpl implements Command {

	private UserLocalService _userLocalService;

	@Activate
	protected void start() {
	      System.out.println("The Liffey command interpreter is active");
	}

	@Deactivate
	protected void stop() {
		System.out.println("The Liffey command interpreter has been turned off");
	}

	public UserLocalService getUserLocalService() {
		return _userLocalService;
	}

	@Reference
	public void setUserLocalService(UserLocalService _userLocalService) {
		this._userLocalService = _userLocalService;
	}

	@Override
	public Object command(String[] args) {
		return Interpreter.execute(getUserLocalService(),args);
	}
}

