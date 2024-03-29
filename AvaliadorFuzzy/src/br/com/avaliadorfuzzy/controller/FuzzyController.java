package br.com.avaliadorfuzzy.controller;

import br.com.avaliadorfuzzy.dao.RegraDAO;
import br.com.avaliadorfuzzy.model.Regra;
import br.com.avaliadorfuzzy.model.Segmento;
import br.com.avaliadorfuzzy.model.Termo;

public class FuzzyController {
	
	private RegraDAO regraDAO;
	private double AlfaTempo;
	private double AlfaConforto;
	private double Menor_alfa;
	private double denominadorRegra = 0;
	private double numeradorRegra = 0;
	private double ValorFinal = 0;
	
	public double getFuzzyClassification(int fatorTempo, int fatorConforto) {

		double centroidRegra = 0;
		double numeradorGeral = 0;
		double denominadorGeral = 0;

		if (regraDAO == null)
			regraDAO = new RegraDAO();

		try {

			// Iterando nas regras

			for (Regra rul : regraDAO.findRegras()) {

				centroidRegra = 0;
				denominadorRegra = 0;
				Menor_alfa = 1;

				// Transformando valores escalares em fuzzy e realizando a infer�ncia

				AlfaTempo = avaliaMI(rul.getTermoByIdTermoTempo(), fatorTempo);
				if (AlfaTempo < Menor_alfa)
					Menor_alfa = AlfaTempo;
				
				AlfaConforto = avaliaMI(rul.getTermoByIdTermoConforto(), fatorConforto);
				if (AlfaConforto < Menor_alfa)
					Menor_alfa = AlfaConforto;

				// _______rebaixa termo de sa�da a altura do menor valor e
				// calcula centr�ide da
				// regra______________________________________
				if (Menor_alfa > 0) {
					// descomente essa linha para ver cada regra que foi ativada
					// no console....
					// printRegra(rul,true);

					// ________calcula centroide da regra e soma ao numerador
					// geral__
					centroidRegra = calculateCentroid(rul.getTermoByIdTermoAvaliacao());

					// somando o centroide da regra ao somatorio de centroides
					numeradorGeral += (centroidRegra * denominadorRegra);

					// somando o denominador da regra ao somatorio de areas
					denominadorGeral += denominadorRegra;
				}
			}

		} catch (Exception e) {
			e.getMessage();
		}

		// setando a classifica��o : pega o somatorio de centroides * area e
		// divide pelo somatorio de areas
		ValorFinal = numeradorGeral / denominadorGeral;

		return (double) ValorFinal;
	}

	// M�todo que realiza a transforma��o dos valores escalares em valores fuzzy
	public double avaliaMI(Termo term, int valueFactor) {

		Segmento activatedSegment = null;
		double pertinence = 0;

		try {
			// ___________itera para buscar o segmento que possui o intervalo do
			// valor selecionado_____
			for (Segmento seg : term.getSegmentos()) {

				if ((seg.getIntervaloEsquerdo() <= valueFactor)
						&& (seg.getIntervaloDireito() >= valueFactor)) {
					activatedSegment = seg;
				}
			}
			if (activatedSegment != null) {

				// __________________________________calculando coef angular e
				// linear_______________________________________
				double coefAngular = calculaCoefAngular(activatedSegment);
				double coefLinear = activatedSegment.getMiEsquerdo()
						- (activatedSegment.getIntervaloEsquerdo() * coefAngular);

				// ______________calcula o pertinence (y =
				// M*x+Q)_____________________________
				pertinence = (coefAngular * valueFactor) + coefLinear;
			}
		} catch (Exception e) {
			e.getMessage();
		}

		return pertinence;
	}

	private double calculaCoefAngular(Segmento seg) {

		double a = seg.getMiDireito() - seg.getMiEsquerdo();
		double b = seg.getIntervaloDireito() - seg.getIntervaloEsquerdo();

		double coefAngular = a / b;

		return coefAngular;

	}

	private double calculateCentroid(Termo consequentTerm) {

		double numeradorSegmento;
		double denominadorSegmento;
		double CentroideRegra = 0;
		numeradorRegra = 0;
		denominadorRegra = 0;

		try {

			for (Segmento seg : consequentTerm.getSegmentos()) {
				numeradorSegmento = 0;
				denominadorSegmento = 0;

				if (seg.getMiEsquerdo() == 1)
					seg.setMiEsquerdo(Menor_alfa);

				if (seg.getMiDireito() == 1)
					seg.setMiDireito((Menor_alfa));

				// _______calculando coef angular e
				// linear_________________________________________
				double Coef_angular = calculaCoefAngular(seg);
				double Coef_linear = seg.getMiEsquerdo() - (seg.getIntervaloEsquerdo() * Coef_angular);

				// __________integral numerador da reta_____________________
				double potencia1 = Math.pow(seg.getIntervaloDireito(), 3);
				double potencia2 = Math.pow(seg.getIntervaloDireito(), 2);
				double potencia3 = Math.pow(seg.getIntervaloEsquerdo(), 3);
				double potencia4 = Math.pow(seg.getIntervaloEsquerdo(), 2);

				double aux1 = (potencia1 * Coef_angular) / 3;
				double aux2 = (potencia2 * Coef_linear) / 2;
				double aux3 = (potencia3 * Coef_angular) / 3;
				double aux4 = (potencia4 * Coef_linear) / 2;

				numeradorSegmento = (aux1 + aux2 - aux3 - aux4);

				// _________integral denominador da reta_________________
				potencia1 = Math.pow(seg.getIntervaloDireito(), 2);
				potencia3 = Math.pow(seg.getIntervaloEsquerdo(), 2);

				aux1 = (potencia1 * Coef_angular) / 2;
				aux2 = (seg.getIntervaloDireito() * Coef_linear);
				aux3 = (potencia3 * Coef_angular) / 2;
				aux4 = (seg.getIntervaloEsquerdo() * Coef_linear);

				denominadorSegmento = (aux1 + aux2 - aux3 - aux4);

				numeradorRegra += numeradorSegmento;
				denominadorRegra += denominadorSegmento;
			}

			CentroideRegra = numeradorRegra / denominadorRegra;

		} catch (Exception e) {
			e.getMessage();
		}

		return CentroideRegra;
	}

}
