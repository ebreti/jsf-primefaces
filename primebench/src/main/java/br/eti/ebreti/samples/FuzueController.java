package br.eti.ebreti.samples;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@SessionScoped
public class FuzueController implements Serializable {

	private static final long serialVersionUID = -6200471252592060488L;

	private Botao[] listaBotoes;

	FuzueController() {
		listaBotoes = new Botao[99];
		for (int i = 0; i < 100; i++) {
/*			listaBotoes[i].setLabel("AA");
			listaBotoes[i].setDisabled("false");
*/		}
	}

	public void click(Number label) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(String.valueOf(label)));
	}

	public String disabled(Number label) {
		return "false";
	}

	public String label(Number label) {
		return "AA";
	}

	public Botao[] getListaBotoes() {
		return listaBotoes;
	}

	public void setListaBotoes(Botao[] listaBotoes) {
		this.listaBotoes = listaBotoes;
	}

}
class Botao {

	private String label;
	private String disabled;

	Botao() {
		this.label = "AA";
		this.disabled = "false";
	}

	Botao(String label, String disabled) {
		this.label = label;
		this.disabled = disabled;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDisabled() {
		return disabled;
	}

	public void setDisabled(String disabled) {
		this.disabled = disabled;
	}

}
