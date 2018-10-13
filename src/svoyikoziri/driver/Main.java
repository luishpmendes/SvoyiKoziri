package svoyikoziri.driver;

import java.util.Arrays;
import java.util.List;

import svoyikoziri.deck.Card;
import svoyikoziri.deck.Rank;
import svoyikoziri.deck.Suit;
import svoyikoziri.engine.DefaultEngine;
import svoyikoziri.engine.Engine;
import svoyikoziri.engine.exception.InvalidPlayException;
import svoyikoziri.engine.exception.MaxPlayTimeExceededException;
import svoyikoziri.engine.exception.NullPlayException;
import svoyikoziri.engine.exception.PlayACardNotInHandException;
import svoyikoziri.engine.exception.PlayANullCardException;
import svoyikoziri.engine.exception.PlayAWorseCardException;
import svoyikoziri.engine.exception.TakeAllCardsAsFirstPlayException;
import svoyikoziri.player.DeterministicDummyPlayer;
import svoyikoziri.player.InvalidPlayer;
import svoyikoziri.player.Player;

/**
 * A classe <code>Main</code> representa uma aplicação em Java.
 * 
 * @author Luis H. P. Mendes
 */
public class Main {
    /**
     * O método main é o ponto de partida para esta aplicação Java.
     * 
     * @param args argumentos passados ao método main.
     */
    public static void main(String[] args) {
        List<Card> deck;
        Player player1, player2;
        Engine engine;

        // Cria um baralho de 32 cartas já embaralhado
        deck = Arrays.asList(
                new Card(Suit.TILES, Rank.ACE),
                new Card(Suit.CLOVERS, Rank.ACE),
                new Card(Suit.CLOVERS, Rank.EIGHT),
                new Card(Suit.HEARTS, Rank.EIGHT),
                new Card(Suit.PIKES, Rank.EIGHT),
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.HEARTS, Rank.QUEEN),
                new Card(Suit.TILES, Rank.QUEEN),
                new Card(Suit.HEARTS, Rank.NINE),
                new Card(Suit.TILES, Rank.NINE),
                new Card(Suit.PIKES, Rank.ACE),
                new Card(Suit.HEARTS, Rank.JACK),
                new Card(Suit.PIKES, Rank.SEVEN),
                new Card(Suit.TILES, Rank.JACK),
                new Card(Suit.HEARTS, Rank.SEVEN),
                new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.CLOVERS, Rank.JACK),
                new Card(Suit.PIKES, Rank.JACK),
                new Card(Suit.PIKES, Rank.TEN),
                new Card(Suit.PIKES, Rank.QUEEN),
                new Card(Suit.CLOVERS, Rank.TEN),
                new Card(Suit.HEARTS, Rank.KING),
                new Card(Suit.TILES, Rank.KING),
                new Card(Suit.CLOVERS, Rank.QUEEN),
                new Card(Suit.CLOVERS, Rank.KING),
                new Card(Suit.PIKES, Rank.KING),
                new Card(Suit.TILES, Rank.TEN),
                new Card(Suit.PIKES, Rank.NINE),
                new Card(Suit.CLOVERS, Rank.NINE),
                new Card(Suit.TILES, Rank.SEVEN),
                new Card(Suit.TILES, Rank.EIGHT),
                new Card(Suit.CLOVERS, Rank.SEVEN));

        // Tenta instanciar um motor entre dois jogadores de naipes trunfos 
        // de mesma cor
        player1 = new DeterministicDummyPlayer(Suit.HEARTS);
        player2 = new DeterministicDummyPlayer(Suit.TILES);

        try {
            engine = new DefaultEngine(player1, player2, deck, 
                    Engine.DEFAULT_MAX_ROUNDS, Engine.DEFAULT_MAX_PLAY_TIME, 
                    false);
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
        }

        // Tenta jogar uma partida com um jogador que faz jogadas nulas
        player1 = new InvalidPlayer(Suit.HEARTS, NullPlayException.class);
        player2 = new DeterministicDummyPlayer(Suit.CLOVERS);

        try {
            engine = new DefaultEngine(player1, player2, deck, 
                    Engine.DEFAULT_MAX_ROUNDS, Engine.DEFAULT_MAX_PLAY_TIME, 
                    true);
            
            engine.playMatch();
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
        }

        // Tenta jogar uma partida com um jogador que joga cartas nulas
        player1 = new InvalidPlayer(Suit.HEARTS, PlayANullCardException.class);
        player2 = new DeterministicDummyPlayer(Suit.PIKES);

        try {
            engine = new DefaultEngine(player1, player2, deck, 
                    Engine.DEFAULT_MAX_ROUNDS, Engine.DEFAULT_MAX_PLAY_TIME, 
                    false);
            
            engine.playMatch();
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
        }

        // Tenta jogar uma partida com um jogador que joga cartas que não 
        // estão em sua mão
        player1 = new InvalidPlayer(Suit.TILES, 
                PlayACardNotInHandException.class);
        player2 = new DeterministicDummyPlayer(Suit.CLOVERS);

        try {
            engine = new DefaultEngine(player1, player2, deck, 
                    Engine.DEFAULT_MAX_ROUNDS, Engine.DEFAULT_MAX_PLAY_TIME, 
                    true);
            
            engine.playMatch();
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
        }

        // Tenta jogar uma partida com um jogador que tenta pegar todas as 
        // cartas da mesa sendo o primeiro a jogar na rodada
        player1 = new InvalidPlayer(Suit.TILES, 
                TakeAllCardsAsFirstPlayException.class);
        player2 = new DeterministicDummyPlayer(Suit.PIKES);

        try {
            engine = new DefaultEngine(player1, player2, deck, 
                    Engine.DEFAULT_MAX_ROUNDS, Engine.DEFAULT_MAX_PLAY_TIME, 
                    false);
            
            engine.playMatch();
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
        }

        // Tenta jogar uma partida com um jogador que jogar uma carta que é 
        // pior do que a carta no topo da pilha de cartas da mesa
        player1 = new DeterministicDummyPlayer(Suit.CLOVERS);
        player2 = new InvalidPlayer(Suit.HEARTS, 
                PlayAWorseCardException.class);

        try {
            engine = new DefaultEngine(player1, player2, deck, 
                    Engine.DEFAULT_MAX_ROUNDS, Engine.DEFAULT_MAX_PLAY_TIME, 
                    true);
            
            engine.playMatch();
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
        }

        // Tenta jogar uma partida com um jogador que lança exceção ao tentar 
        // fazer sua jogada
        player1 = new InvalidPlayer(Suit.CLOVERS, Exception.class);
        player2 = new DeterministicDummyPlayer(Suit.TILES);

        try {
            engine = new DefaultEngine(player1, player2, deck, 
                    Engine.DEFAULT_MAX_ROUNDS, Engine.DEFAULT_MAX_PLAY_TIME, 
                    false);
            
            engine.playMatch();
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
        }

        // Tenta jogar uma partida com um jogador que demora mais do que o 
        // permitido para fazer a sua jogada
        player1 = new InvalidPlayer(Suit.PIKES, 
                MaxPlayTimeExceededException.class);
        player2 = new DeterministicDummyPlayer(Suit.HEARTS);

        try {
            engine = new DefaultEngine(player1, player2, deck, 
                    Engine.DEFAULT_MAX_ROUNDS, Engine.DEFAULT_MAX_PLAY_TIME, 
                    true);
            
            engine.playMatch();
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
        }
        
        // Tenta jogar uma partida com um jogador que faz jogadas inválidas
        player1 = new InvalidPlayer(Suit.PIKES, InvalidPlayException.class);
        player2 = new DeterministicDummyPlayer(Suit.TILES);

        try {
            engine = new DefaultEngine(player1, player2, deck, 
                    Engine.DEFAULT_MAX_ROUNDS, Engine.DEFAULT_MAX_PLAY_TIME, 
                    false);
            
            engine.playMatch();
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
        }

        // Joga uma partida entre dois jogadores "ingênuos" determinísticos
        player1 = new DeterministicDummyPlayer(Suit.HEARTS);
        player2 = new DeterministicDummyPlayer(Suit.CLOVERS);

        try {
            engine = new DefaultEngine(player1, player2, deck, 
                    Engine.DEFAULT_MAX_ROUNDS, Engine.DEFAULT_MAX_PLAY_TIME, 
                    true);
            
            engine.playMatch();
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
        }
        
        // Joga uma partida entre dois jogadores "ingênuos" determinísticos
        player1 = new DeterministicDummyPlayer(Suit.PIKES);
        player2 = new DeterministicDummyPlayer(Suit.TILES);

        try {
            engine = new DefaultEngine(player1, player2, deck, 
                    Engine.DEFAULT_MAX_ROUNDS, Engine.DEFAULT_MAX_PLAY_TIME, 
                    false);
            
            engine.playMatch();
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
        }
    }
}
