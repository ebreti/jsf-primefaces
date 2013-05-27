package br.eti.ebreti.samples;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 * User.
 * 
 * @author fmarianoc
 */
@Named
@SessionScoped
public class User implements Serializable {

	private static final long serialVersionUID = -5534850140856145409L;
	
	/**
	 * The user state.
	 */
	private UserState estado;

	/**
	 * The user login.
	 * Must be one valid email address.
	 */
	private String login;

	/**
	 * The password.
	 */
	private String password;

	/**
	 * The nickname.
	 */
	private String nickname;

	/**
	 * The level.
	 */
	private LevelEnum level;

	@PostConstruct
	public void reset() {
		this.estado = UserState.DESCONHECIDO;
		this.login = null;
		this.password = null;
		this.nickname = null;
	}

	public void loggedIn() {
		this.estado = UserState.CONHECIDO;
		this.level = LevelEnum.EASY;
	}

	public void loggedOut() {
		this.estado = UserState.DESCONHECIDO;
		this.level = LevelEnum.NORMAL;
	}

	public void validateEmailAddress(FacesContext context,
			UIComponent toValidate, Object value) {
		String input = (String) value;
		EmailValidator emailValidator = new EmailValidator();
		if (!emailValidator.validate(input)) {
			((UIInput) toValidate).setValid(false);
			FacesMessage message = new FacesMessage(input 
					+ " não parece ser um endereço de email válido." 
					+ " Acerte e tente de novo.");
			context.addMessage(toValidate.getClientId(context), message);
		}
	}

	public void validatePassword(FacesContext context,
			UIComponent toValidate, Object value) {
		String input = (String) value;
		PasswordValidator passwordValidator = new PasswordValidator();
		if (!passwordValidator.validate(input)) {
			((UIInput) toValidate).setValid(false);
			FacesMessage message = new FacesMessage(input 
					+ " não obedece as regras de formação para uma senha forte." 
					+ " Acerte e tente de novo.");
			context.addMessage(toValidate.getClientId(context), message);
		}
	}

	public UserState getEstado() {
		return estado;
	}

	public void setEstado(UserState estado) {
		this.estado = estado;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public LevelEnum getLevel() {
		return level;
	}

	public void setLevel(LevelEnum level) {
		this.level = level;
	}

}
