/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.eti.ebreti.samples;

import java.io.Serializable;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * <p>
 * {@link Game} contains all the business logic for the application, and also
 * serves as the controller for the JSF view.
 * </p>
 * <p>
 * It contains properties for the <code>number</code> to be guessed, the current
 * <code>guess</code>, the <code>smallest</code> and <code>biggest</code>
 * numbers guessed so far (as this is a higher/lower game we can prevent them
 * entering numbers that they should know are wrong), and the number of
 * <code>remainingGuesses</code>.
 * </p>
 * <p>
 * The {@link #check()} method, and {@link #reset()} methods provide the
 * business logic whilst the
 * {@link #validateNumberRange(FacesContext, UIComponent, Object)} method
 * provides feedback to the user.
 * </p>
 *
 * @author Pete Muir
 * @contributor fmarianoc
 *
 */
@Named
@SessionScoped
public class Game implements Serializable {

	private static final long serialVersionUID = -4922390286153874072L;

	/**
	 * Max number of tries.
	 */
	static final int CHANCES = 10;

	/**
	 * The game state.
	 */
	private GameState estado;

	/**
	 * Score.
	 */
	private int pontos;

	/**
	 * Timestamp.
	 */
	private Calendar t1;

	/**
	 * The number that the user needs to guess.
	 */
	private int number;

	/**
	 * The users latest guess.
	 */
	private int guess;

	/**
	 * The smallest number guessed so far (so we can track the valid guess
	 * range).
	 */
	private int smallest;

	/**
	 * The largest number guessed so far.
	 */
	private int biggest;

	/**
	 * The number of guesses remaining.
	 */
	private int remainingGuesses;

	/**
	 * The maximum number we should ask them to guess.
	 */
	@Inject
	@MaxNumber
	private int maxNumber;

	/**
	 * The random number to guess.
	 */
	@Inject
	@Random
	Instance<Integer> randomNumber;

	public Game() {
		this.pontos = 0;
	}

	/**
	 * Check whether the current guess is correct, and update the
	 * biggest/smallest guesses as needed.
	 * Give feedback to the user if they are correct.
	 */
	public void check() {
		Calendar tempo = Calendar.getInstance();
		long delta = tempo.getTimeInMillis() - this.t1.getTimeInMillis();
		this.pontos += premiaRapidez(delta); 
		if (guess == number) {
			FacesContext.getCurrentInstance().addMessage(null, getWinnerMessage());
			this.pontos += premiaPrecisao();
			this.estado = GameState.TERMINADO;
			this.remainingGuesses--;
			return;
		}
		this.remainingGuesses--;
		if (this.remainingGuesses <= 0) {
			FacesMessage finalMessage = new FacesMessage("Que pena, acabaram suas chances... Tente de novo!");
			FacesContext.getCurrentInstance().addMessage(null, finalMessage);
			this.estado = GameState.TERMINADO;
			return;
		}
		if (this.guess > this.number) {
			this.biggest = this.guess - 1;
	        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Escolha um número menor!"));
		} else {
			this.smallest = this.guess + 1;
	        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Escolha um número maior!"));
		}
		this.t1 = Calendar.getInstance();
	}

	private long premiaRapidez(long delta) {
		if (delta <= 3500L) {
			return (5000L - delta);
		} else if (delta <= 6500L) {
			return (long)(800L + (7000L - delta) * .4);
		}
		return (long)((delta % 1000L) * .8);
	}

	private long premiaPrecisao() {
		return this.remainingGuesses * (this.biggest - this.smallest + 1) * 30;
	}

	private FacesMessage getWinnerMessage() {
		FacesMessage winnerMessage;
		if (this.remainingGuesses == Game.CHANCES) {
			winnerMessage = new FacesMessage("Uau! De prima! Consegue repetir?");
			this.pontos += 10000;
		} else if (this.remainingGuesses == 1) {
			winnerMessage = new FacesMessage("Por pouco! Acertou na última. Treine mais...");
		} else if (this.remainingGuesses < Game.CHANCES / 3) {
			winnerMessage = new FacesMessage("Ufa! Acertou nas últimas!");
		} else if (this.remainingGuesses > 2 * (Game.CHANCES / 3)) {
			winnerMessage = new FacesMessage("Boa! Está pegando a manha...");
			this.pontos += 2000;
		} else {
			winnerMessage = new FacesMessage("Parabéns! Você acertou na "
					+ (Game.CHANCES + 1 - this.remainingGuesses) + "ª tentativa.");
		}
		return winnerMessage;
	}

	/**
	 * Reset the game, by putting all values back to their defaults, and getting
	 * a new random number. We also call this method when the user starts
	 * playing for the first time using {@linkplain PostConstruct
	 * @PostConstruct} to set the initial values.
	 */
	@PostConstruct
	public void reset() {
		this.estado = GameState.PRONTO;
		this.smallest = 0;
		this.guess = 0;
		this.remainingGuesses = Game.CHANCES;
		this.biggest = maxNumber;
		this.number = randomNumber.get();
		this.t1 = Calendar.getInstance();
	}

	/**
	 * A JSF validation method which checks whether the guess is valid. It might
	 * not be valid because there are no guesses left, or because the guess is
	 * not in range.
	 * 
	 */
	public void validateNumberRange(FacesContext context,
			UIComponent toValidate, Object value) {
		int input = (Integer) value;

		if (input < this.smallest || input > this.biggest) {
			((UIInput) toValidate).setValid(false);

			FacesMessage message = new FacesMessage("Não serve. Tente de novo.");
			context.addMessage(toValidate.getClientId(context), message);
		}
	}

	public GameState getEstado() {
		return estado;
	}

	public void setEstado(GameState estado) {
		this.estado = estado;
	}

	public int getPontos() {
		return pontos;
	}

	public void setPontos(int pontos) {
		this.pontos = pontos;
	}

	public Calendar getT1() {
		return t1;
	}

	public void setT1(Calendar t1) {
		this.t1 = t1;
	}

	public int getNumber() {
		return number;
	}

	public int getGuess() {
		return guess;
	}

	public void setGuess(int guess) {
		this.guess = guess;
	}

	public int getSmallest() {
		return smallest;
	}

	public int getBiggest() {
		return biggest;
	}

	public int getRemainingGuesses() {
		return remainingGuesses;
	}
}
