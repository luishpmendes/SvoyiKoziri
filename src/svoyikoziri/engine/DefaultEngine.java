package svoyikoziri.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import svoyikoziri.deck.Card;
import svoyikoziri.deck.Color;
import svoyikoziri.deck.Rank;
import svoyikoziri.deck.Suit;
import svoyikoziri.engine.exception.InvalidPlayException;
import svoyikoziri.engine.exception.MaxPlayTimeExceededException;
import svoyikoziri.engine.exception.NullPlayException;
import svoyikoziri.engine.exception.PlayACardNotInHandException;
import svoyikoziri.engine.exception.PlayANullCardException;
import svoyikoziri.engine.exception.PlayAWorseCardException;
import svoyikoziri.engine.exception.SameTrumpColorsException;
import svoyikoziri.engine.exception.TakeAllCardsAsFirstPlayException;
import svoyikoziri.player.Player;

/**
 * A classe <code>DefaultEngine</code> representa um motor padrão do jogo de 
 * cartas Svoyi Koziri.
 *
 * @author Luis H. P. Mendes
 */
public class DefaultEngine extends Engine {
    /**
     * O Jogador1.
     */
    private final Player player1;

    /**
     * O Jogador2.
     */
    private final Player player2;

    /**
     * Cartas na mão do Jogador1.
     */
    private List<Card> hand1;

    /**
     * Cartas na mão do Jogador2.
     */
    private List<Card> hand2;

    /**
     * A pilha de cartas da mesa.
     */
    private Stack<Card> cardsOnTable;

    /**
     * Lista das jogadas.
     */
    private List<Play> plays;

    /**
     * O número máximo de rodadas da partida.
     */
    private final int maxRounds;

    /**
     * O número da rodada atual da partida, no intervalo [1, maxRound].
     */
    private int currentRound;

    /**
     * O tempo máximo para realizar cada jogada em nanosegundos.
     */
    private final long maxPlayTime;

    /**
     * Flag para verbosidade ligada (true) ou desligada (false).
     */
    private final boolean verbose;

    /**
     * Inicializa um objeto de <code>DefaultEngine</code> recém-criado.
     *
     * @param player1     O Jogador1.
     * @param player2     O Jogador2.
     * @param minRank     O menor rank a ser considerado no baralho, ou seja, 
     *                    o intervalo de ranks será [minRank, ACE].
     * @param seed        A semente do gerador de números aleatórios.
     * @param maxRounds   O número máximo de rodadas da partida.
     * @param maxPlayTime O tempo máximo em nanosegundos que cada jogador tem 
     *                    para realizar sua jogada a cada turno.
     * @param verbose     A flag de verbosidade.
     */
    public DefaultEngine(Player player1, Player player2, Rank minRank, 
            long seed, int maxRounds, long maxPlayTime, boolean verbose) 
                    throws SameTrumpColorsException {
        if (player1.getTrump().getColor().equals(player2.getTrump().getColor())) {
            throw new SameTrumpColorsException(player1.getTrump(), 
                    player2.getTrump());
        }

        this.player1 = player1;
        this.player2 = player2;
        this.cardsOnTable = new Stack<Card>();
        this.plays = new ArrayList<Play>();
        this.maxRounds = maxRounds;
        this.currentRound = 0;
        this.maxPlayTime = maxPlayTime;
        this.verbose = verbose;

        this.initialize(DefaultEngine.generateDeck(minRank), new Random(seed));
    }

    /**
     * Inicializa um objeto de <code>DefaultEngine</code> recém-criado.
     *
     * @param player1     O Jogador1.
     * @param player2     O Jogador2.
     * @param deck        O baralho a ser utilizado na partida, já embaralhado.
     * @param maxRounds   O número máximo de rodadas da partida.
     * @param maxPlayTime O tempo máximo em nanosegundos que cada jogador tem 
     *                    para realizar sua jogada a cada turno.
     * @param verbose     A flag de verbosidade.
     */
    public DefaultEngine(Player player1, Player player2, List<Card> deck, 
            int maxRounds, long maxPlayTime, boolean verbose) 
                    throws SameTrumpColorsException {
        if (player1.getTrump().getColor().equals(player2.getTrump().getColor())) {
            throw new SameTrumpColorsException(player1.getTrump(), 
                    player2.getTrump());
        }

        this.player1 = player1;
        this.player2 = player2;
        this.cardsOnTable = new Stack<Card>();
        this.plays = new ArrayList<Play>();
        this.maxRounds = maxRounds;
        this.currentRound = 0;
        this.maxPlayTime = maxPlayTime;
        this.verbose = verbose;

        this.initialize(deck);
    }

    /**
     * Cria um baralho do jogo Svoyi Koziri com os ranks passados por parâmetro.
     *
     * @param minRank O menor rank a ser considerado no baralho, ou seja, 
     *                o intervalo de ranks será [minRank, ACE].
     * 
     * @return O baralho padrão do jogo Svoyi Koziri.
     */
    private static List<Card> generateDeck(Rank minRank) {
        List<Card> cards = new ArrayList<Card>();

        // Para cada naipe do jogo
        for (Suit suit : Suit.values()) {
            // Para cada rank passado por parâmetro
            for (Rank rank : Rank.values()) {
                // Se o rank for mais que minRank
                if (rank.compareTo(minRank) >= 0) {
                    // Adiciona uma nova carta ao baralho com este naipe e rank
                    cards.add(new Card(suit, rank));
                }
            }
        }

        return cards;
    }

    /**
     * Processa a fase de inicialização de uma partida do jogo de cartas 
     * Svoyi Koziri.
     *
     * @param deck O baralho utilizado na partida.
     */
    private void initialize(List<Card> deck) {
        List<Card> deckAux = new ArrayList<Card>(deck);
        Suit nonTrumpPlayer1;

        if (this.player1.getTrump().getColor().equals(Color.RED)) {
            if (this.player1.getTrump().equals(Suit.HEARTS)) {
                nonTrumpPlayer1 = Suit.TILES;
            } else {
                nonTrumpPlayer1 = Suit.HEARTS;
            }
        } else {
            if (this.player1.getTrump().equals(Suit.CLOVERS)) {
                nonTrumpPlayer1 = Suit.PIKES;
            } else {
                nonTrumpPlayer1 = Suit.CLOVERS;
            }
        }

        this.hand1 = new ArrayList<Card>(deckAux.size() / 2);
        this.hand2 = new ArrayList<Card>(deckAux.size() / 2);

        // Para cada carta na primeira metade do baralho
        for (Card card : deckAux.subList(0, deckAux.size() / 2)) {
            // Se o naipe da carta for da mesma cor do naipe trunfo do Jogador1
            if (card.getSuit().getColor().equals(this.player1.getTrump().getColor())) {
                // Adiciona a carta à mão do Jogador1
                this.hand1.add(card);
            }
        }

        // Remove do baralhos as cartas que já estão na mão do Jogador1
        deckAux.removeAll(this.hand1);

        // Para cada carta que ainda está no baralho
        for (Card card : deckAux) {
            // Se o naipe da carta for da mesma cor que o naipe trunfo do Jogador2
            if (card.getSuit().getColor().equals(this.player2.getTrump().getColor())) {
                // Se a mão do Jogador1 possui uma carta igual (porém com a outra cor)
                if (this.hand1.contains(new Card(card.getSuit().equals(
                        this.player2.getTrump()) ? this.player1.getTrump() : 
                            nonTrumpPlayer1, card.getRank()))) {
                    // Adiciona a carta à mão do Jogador2
                    this.hand2.add(card);
                }
            }
        }

        // Remove do baralhos as cartas que já estão na mão do Jogador2
        deckAux.removeAll(this.hand2);

        // Para cada carta que ainda está no baralho
        for (Card card : deckAux) {
            // Se o naipe da carta for da mesma cor que o naipe trunfo do 
            // Jogador2
            if (card.getSuit().getColor().equals(this.player2.getTrump().getColor())) {
                // Adiciona a carta à mão do Jogador1
                this.hand1.add(card);
            }
        }

        // Remove do baralhos as cartas que já estão na mão do Jogador1
        deckAux.removeAll(this.hand1);

        // Adiciona o restante das cartas (cujos naipes são da cor do naipe 
        // trunfo do Jogador1) à mão do Jogador2
        this.hand2.addAll(deckAux);
    }

    /**
     * Processa a fase de inicialização de uma partida do jogo de cartas 
     * Svoyi Koziri.
     *
     * @param deck O baralho utilizado na partida.
     * @param rnd O gerador de números aleatórios.
     */
    private void initialize(List<Card> deck, Random rnd) {
        // Embaralha as cartas do baralho
        Collections.shuffle(deck, rnd);

        this.initialize(deck);
    }

    /**
     * Recupera uma cópia imutável das cartas da mão do Jogador1.
     *
     * @return Uma cópia imutável das cartas da mão do Jogador1.
     */
    private List<Card> getUnmodifiableHand1() {
        return Collections.unmodifiableList(this.hand1);
    }

    /**
     * Recupera uma cópia imutável das cartas da mão do Jogador2.
     *
     * @return Uma cópia imutável das cartas da mão do Jogador2.
     */
    private List<Card> getUnmodifiableHand2() {
        return Collections.unmodifiableList(this.hand2);
    }

    /**
     * Recupera a lista de cartas de um jogador.
     *
     * @param player O jogador em questão.
     *
     * @return A lista de cartas de um jogador.
     */
    private List<Card> getHandOfPlayer(Player player) {
        if (player == this.player1) {
            return this.hand1;
        } else if (player == this.player2) {
            return this.hand2;
        }

        return null;
    }

    /**
     * Processa uma jogada de um jogador e, caso seja o último turno da rodada, 
     * retorna true se o jogador vencer e false caso contrário.
     *
     * @param player    O jogador que fez a jogada
     * @param play      A jogada feita pelo jogador
     * @param firstPlay Um valor booleano que indica se a jogada é a primeira 
     *                  da rodada atual.
     *
     * @return Caso seja o último turno da rodada, 
     *         retorna true se o jogador vencer e false caso contrário
     */
    private boolean processPlay(Player player, Play play, boolean firstPlay) 
            throws NullPlayException, PlayANullCardException, 
            PlayACardNotInHandException, TakeAllCardsAsFirstPlayException, 
            PlayAWorseCardException {
        boolean result = false;

        // Se a jogada for nula
        if (play == null) {
            // Lança uma NullPlayException
            throw new NullPlayException(player.equals(this.player1));
        } else { // Senão
            // A jogada não é nula
            // Se for a primeira jogada da rodada
            if (firstPlay) {
                // Se a jogada for do tipo jogar uma carta
                if (play.getType().equals(PlayType.PLAYACARD)) {
                    // Se a carta jogada for null
                    if (play.getCard() == null) {
                        // Lança uma PlayANullCardException
                        throw new PlayANullCardException(
                                player.equals(this.player1));
                    } else { // Senão
                        // A carta jogada não é nula
                        // Se removeu a carta da mão do jogador
                        if (this.getHandOfPlayer(player).remove(
                                play.getCard())) {
                            // Imprime a jogada
                            this.println(Engine.getValidPlayMessage(
                                    player.equals(this.player1), play));
                            // Adiciona a carta ao topo da pilha de cartas 
                            // da mesa
                            this.cardsOnTable.push(play.getCard());
                        } else { // Senão
                            // O jogador não possuia a carta que jogou
                            // Lança uma PlayACardNotInHandException
                            throw new PlayACardNotInHandException(
                                    player.equals(this.player1), 
                                    play.getCard());
                        }
                    }
                } else { // Senão
                    // A jogada é do tipo pegar todas as cartas
                    // Lança uma TakeAllCardsAsFirstPlayException
                    throw new TakeAllCardsAsFirstPlayException(
                            player.equals(this.player1));
                }
            } else { // Senão
                // É a segunda jogada da rodada
                // Se a jogada for do tipo jogar uma carta
                if (play.getType().equals(PlayType.PLAYACARD)) {
                    // Se a carta jogada for null
                    if (play.getCard() == null) {
                        // Lança uma PlayANullCardException
                        throw new PlayANullCardException(
                                player.equals(this.player1));
                    } else { // Senão
                        // A carta jogada não é nula
                        // Se a pilha de cartas da mesa está vazia
                        if (this.cardsOnTable.isEmpty() 
                                // ou a carta jogada é do mesmo naipe que a 
                                // carta no topo da pilha de cartas da mesa
                                || (play.getCard().getSuit().equals(
                                        this.cardsOnTable.peek().getSuit()) 
                                        // e a carta jogada é maior do que a carta 
                                        // no topo da pilha de cartas da mesa
                                        && play.getCard().compareTo(
                                                this.cardsOnTable.peek()) > 0) 
                                // ou a carta jogada não é do mesmo naipe que a 
                                // carta no topo da pilha de cartas da mesa
                                || (!play.getCard().getSuit().equals(
                                        this.cardsOnTable.peek().getSuit()) 
                                        // e a carta jogada é do naipe trunfo do jogador
                                        && play.getCard().getSuit().equals(
                                                player.getTrump()))) {
                            // Se removeu a carta da mão do jogador
                            if (this.getHandOfPlayer(player).remove(
                                    play.getCard())) {
                                // Imprime a jogada
                                this.println(Engine.getValidPlayMessage(
                                        player.equals(this.player1), play));
                                // Adiciona a carta ao topo da pilha de cartas 
                                // da mesa
                                this.cardsOnTable.push(play.getCard());
                                // E retorna true
                                result = true;
                            } else { // Senão
                                // O jogador não possuia a carta que jogou
                                // Lança uma PlayACardNotInHandException
                                throw new PlayACardNotInHandException(
                                        player.equals(this.player1), 
                                        play.getCard());
                            }
                        } else { // Senão
                            // A carta jogada não é melhor do que 
                            // a carta no topo da pilha de cartas da mesa
                            // Lança uma PlayAWorseCardException
                            throw new PlayAWorseCardException(
                                    player.equals(this.player1), 
                                    play.getCard(), this.cardsOnTable.peek());
                        }
                    }
                } else { // Senão
                    // A jogada é do tipo pegar todas as cartas
                    // Imprime a jogada
                    this.println(Engine.getValidPlayMessage(
                            player.equals(this.player1), play));
                    // Enquanto a pilha de cartas da mesa não estiver vazia
                    while (!this.cardsOnTable.empty()) {
                        // Remove uma carta do topo da pilha e a adiciona à 
                        // mão do jogador
                        this.getHandOfPlayer(player).add(
                                this.cardsOnTable.pop());
                    }
                }
            }

            // Adiciona a jogada no final da lista de jogadas
            this.plays.add(play);
        }

        return result;
    }

    /**
     * Caso a flag de verbosidade esteja ativa, imprime um objeto na saída 
     * padrão.
     *
     * @param obj Objeto a ser impresso.
     */
    @Override
    protected void println(Object obj) {
        if (this.verbose) {
            System.out.println(obj);
        }
    }

    /**
     * Recupera o naipe trunfo do Jogador1.
     *
     * @return O naipe trunfo do Jogador1.
     */
    @Override
    public Suit getPlayer1Trump() {
        return this.player1.getTrump();
    }

    /**
     * Recupera o naipe trunfo do Jogador2.
     *
     * @return O naipe trunfo do Jogador2.
     */
    @Override
    public Suit getPlayer2Trump() {
        return this.player2.getTrump();
    }

    /**
     * Recupera uma cópia imutável das cartas da mão de um jogador.
     *
     * @param player O jogador em questão.
     *
     * @return Uma cópia imutável das cartas da mão de um jogador.
     */
    @Override
    public List<Card> getUnmodifiableHandOfPlayer(Player player) {
        if (player == this.player1) {
            return this.getUnmodifiableHand1();
        } else if (player == this.player2) {
            return this.getUnmodifiableHand2();
        }

        return null;
    }

    /**
     * Recupera uma cópia da pilha de cartas da mesa.
     *
     * @return Uma cópia da pilha de cartas da mesa.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Stack<Card> getCardsOnTable() {
        return (Stack<Card>) this.cardsOnTable.clone();
    }

    /**
     * Recupera uma cópia imutável da lista de jogadas.
     *
     * @return Uma cópia imutável da lista de jogadas.
     */
    @Override
    public List<Play> getUnmodifiablePlays() {
        return Collections.unmodifiableList(this.plays);
    }

    /**
     * Recupera o número máximo de rodadas do jogo.
     *
     * @return O número máximo de rodadas do jogo.
     */
    @Override
    public int getMaxRounds() {
        return this.maxRounds;
    }

    /**
     * Recupera o número da rodada atual do jogo.
     *
     * @return O número da rodada atual do jogo.
     */
    @Override
    public int getCurrentRound() {
        return this.currentRound;
    }

    /**
     * Joga uma partida do jogo de cartas Svoyi Koziri.
     * Retorna o jogador vencedor, ou null em caso de empate.
     *
     * @return O jogador vencedor, ou null em caso de empate.
     *
     * @throws NullPlayException                Lança uma <code>NullPlayException</code> caso um 
     *                                          jogador realize uma jogada nula.
     * @throws PlayANullCardException           Lança uma <code>PlayANullCardException</code> 
     *                                          caso um jogador jogue uma carta nula.
     * @throws PlayACardNotInHandException      Lança uma <code>PlayACardNotInHandException</code> 
     *                                          caso um jogador jogue uma carta que não esteja em 
     *                                          sua mão.
     * @throws TakeAllCardsAsFirstPlayException Lança uma <code>TakeAllCardsAsFirstPlayException</code> 
     *                                          caso um jogador faça uma jogada do tipo 
     *                                          TAKEALLCARDS como a primera jogada de uma rodada. 
     * @throws PlayAWorseCardException          Lança uma <code>PlayAWorseCardException</code> 
     *                                          caso um jogador jogue uma carta pior do que a 
     *                                          carta que está no topo da pilha de cartas da mesa.
     * @throws MaxPlayTimeExceededException     Lança uma <code>MaxPlayTimeExceededException</code> 
     *                                          caso um jogador exceda o tempo máximo para 
     *                                          realizar uma jogada.
     * @throws InvalidPlayException             Lança uma <code>InvalidPlayException</code> caso 
     *                                          um jogador realize uma jogada inválida, isto é, 
     *                                          quando um jogador lança uma exceção ao realizar 
     *                                          sua jogada (por exemplo, 
     *                                          ArrayIndexOutOfBoundsException ou 
     *                                          NullPointerException).
     */
    @Override
    public Player playMatch() 
            throws NullPlayException, PlayANullCardException, 
            PlayACardNotInHandException, TakeAllCardsAsFirstPlayException, 
            PlayAWorseCardException, MaxPlayTimeExceededException, 
            InvalidPlayException {
        Player winner = null;
        boolean player1First = true;
        int roundsWonByPlayer1, roundsWonByPlayer2;

        // Imprime o naipe trunfo de cada jogador
        this.println(Engine.getPlayerTrumpMessage(true, 
                this.player1.getTrump()));
        this.println(Engine.getPlayerTrumpMessage(false, 
                this.player2.getTrump()));

        // Enquanto não atingir o limite de rodadas e os dois jogadores 
        // tiverem cartas na mão
        for (this.currentRound = 1, roundsWonByPlayer1 = roundsWonByPlayer2 = 0; 
                this.currentRound <= this.maxRounds 
                        && !this.hand1.isEmpty() 
                        && !this.hand2.isEmpty(); 
                this.currentRound++) {
            // Imprime o número da rodada atual
            this.println(Engine.getRoundNumberMessage(this.currentRound, 
                    this.maxRounds));

            // Imprime as cartas na mão do Jogador1 em ordem
            List<Card> sortedHand1 = new ArrayList<Card>(this.hand1);
            Collections.sort(sortedHand1);
            this.println(Engine.getNumberOfCardsOnPlayersHandMessage(true, 
                    sortedHand1.size()));
            for (Card card : sortedHand1) {
                this.println(card);
            }

            // Imprime as cartas na mão do Jogador2 em ordem
            List<Card> sortedHand2 = new ArrayList<Card>(this.hand2);
            Collections.sort(sortedHand2);
            this.println(Engine.getNumberOfCardsOnPlayersHandMessage(false, 
                    sortedHand2.size()));
            for (Card card : sortedHand2) {
                this.println(card);
            }

            // Imprime as cartas na pilha de cartas da mesa
            this.println(Engine.getNumberOfCardsOnCardsOnTableMessage(
                    this.cardsOnTable.size()));
            for (Card card : this.cardsOnTable) {
                this.println(card);
            }

            // Se o Jogador1 começa a rodada
            if (player1First) {
                this.println(Engine.getPlayerStartsRoundMessage(true));
                // Computa o momento atual
                long startTime1 = System.nanoTime();
                Play play1;

                try {
                    // O Jogador1 tenta fazer sua jogada
                    play1 = this.player1.playRound(true, this);
                } catch (Exception e) {
                    // Se o Jogador1 não conseguiu fazer sua jogada
                    // Lança uma InvalidPlayException
                    throw new InvalidPlayException(true);
                }

                // Computa o tempo que o Jogador1 demorou para fazer sua jogada
                long deltaTime1 = System.nanoTime() - startTime1;

                // Se o Jogador1 demorou mais do que o permitido para fazer 
                // sua jogada
                if (deltaTime1 > this.maxPlayTime) {
                    // Lança uma MaxPlayTimeExceededException
                    throw new MaxPlayTimeExceededException(true);
                }

                // Processa a jogada do Jogador1
                this.processPlay(this.player1, play1, true);

                // Se o Jogador1 não tiver mais cartas na mão
                if (this.hand1.isEmpty()) {
                    // O Jogador1 vence a partida
                    winner = this.player1;
                    break;
                }

                // Computa o momento atual
                long startTime2 = System.nanoTime();
                Play play2;

                try {
                    // O Jogador2 tenta fazer sua jogada
                    play2 = this.player2.playRound(false, this);
                } catch (Exception e) {
                    // Se o Jogador2 não conseguiu fazer sua jogada
                    // Lança uma InvalidPlayException
                    throw new InvalidPlayException(false);
                }

                // Computa o tempo que o Jogador2 demorou para fazer sua jogada
                long deltaTime2 = System.nanoTime() - startTime2;

                // Se o Jogador2 demorou mais do que o permitido par fazer 
                // sua jogada
                if (deltaTime2 > this.maxPlayTime) {
                    // Lança uma MaxPlayTimeExceededException
                    throw new MaxPlayTimeExceededException(false);
                }

                // Processa a jogada do Jogador2
                // Se o Jogador2 ganhou
                if (this.processPlay(this.player2, play2, false)) {
                    // ele começa a próxima rodada
                    player1First = false;
                    // e incrementa o contador de rodadas ganhas pelo Jogador2
                    roundsWonByPlayer2++;
                    this.println(Engine.getPlayerWinsRoundMessage(false));
                } else { // Senão
                    // incrementa o contador de rodadas ganhas pelo Jogador1
                    roundsWonByPlayer1++;
                    this.println(Engine.getPlayerWinsRoundMessage(true));
                }
            } else { // Se o Jogador2 começa a rodada
                this.println(Engine.getPlayerStartsRoundMessage(false));
                // Computa o momento atual
                long startTime2 = System.nanoTime();
                Play play2;

                try {
                    // O Jogador2 tenta fazer sua jogada
                    play2 = this.player2.playRound(true, this);
                } catch (Exception e) {
                    // Se o Jogador2 não conseguiu fazer sua jogada
                    // Lança uma InvalidPlayException
                    throw new InvalidPlayException(false);
                }

                // Computa o tempo que o Jogador2 demorou para fazer sua jogada
                long deltaTime2 = System.nanoTime() - startTime2;

                // Se o Jogador2 demorou mais do que o permitido para fazer 
                // sua jogada
                if (deltaTime2 > this.maxPlayTime) {
                    // Lança uma MaxPlayTimeExceededException
                    throw new MaxPlayTimeExceededException(false);
                }

                // Processa a jogada do Jogador2
                this.processPlay(this.player2, play2, true);

                // Se o Jogador2 não tiver mais cartas na mão
                if (this.hand2.isEmpty()) {
                    // O Jogador2 vence a partida
                    winner = this.player2;
                    break;
                }

                // Computa o momento atual
                long startTime1 = System.nanoTime();
                Play play1;

                try {
                    // O Jogador1 tenta fazer sua jogada
                    play1 = this.player1.playRound(false, this);
                } catch (Exception e) {
                    // Se o Jogador1 não conseguiu fazer sua jogada
                    // O Jogador2 vence a partida
                    // Lança uma InvalidPlayException
                    throw new InvalidPlayException(true);
                }

                // Computa o tempo que o Jogador1 demorou para fazer sua jogada
                long deltaTime1 = System.nanoTime() - startTime1;

                // Se o Jogador1 demorou mais do que o permitido para fazer 
                // sua jogada
                if (deltaTime1 > this.maxPlayTime) {
                    // Lança uma MaxPlayTimeExceededException
                    throw new MaxPlayTimeExceededException(true);
                }

                // Processa a jogada do Jogador1
                // Se o Jogador1 ganhou
                if (this.processPlay(this.player1, play1, false)) {
                    // ele começa a próxima rodada
                    player1First = true;
                    // e incrementa o contador de rodadas ganhas pelo Jogador1
                    roundsWonByPlayer1++;
                    this.println(Engine.getPlayerWinsRoundMessage(true));
                } else { // Senão
                    // incrementa o contador de rodadas ganhas pelo Jogador2
                    roundsWonByPlayer2++;
                    this.println(Engine.getPlayerWinsRoundMessage(false));
                }
            }
        }

        // Se o vencedor ainda não estiver definido
        if (winner == null) {
            // Se o Jogador1 tem menos cartas na mão que o Jogador2
            if (this.hand1.size() < this.hand2.size()) {
                // O Jogador1 vence a partida
                winner = this.player1;
            // Se o Jogador1 tem mais cartas na mão que o Jogador2
            } else if (this.hand1.size() > this.hand2.size()) {
                // O Jogador2 vence a partida
                winner = this.player2;
            // Se os jogadores tem a mesma quantidade de cartas na mão e o 
            // Jogador1 ganhou mais rodadas que o Jogador2
            } else if (roundsWonByPlayer1 > roundsWonByPlayer2) {
                // O Jogador1 vence a partida
                winner = this.player1;
            // Se os jogadores tem a mesma quantidade de cartas na mão e o 
            // Jogador1 ganhou menos rodadas que o Jogador2
            } else if (roundsWonByPlayer1 < roundsWonByPlayer2) {
                // O Jogador2 vence a partida
                winner = this.player2;
            }
        }

        if (winner == null) {
            this.println(Engine.PLAYERS_DREW_MESSAGE);
        } else {
            this.println(Engine.getWinnerPlayerMessage(
                    winner.equals(this.player1)));
        }

        // Retorna o vencedor
        return winner;
    }
}
