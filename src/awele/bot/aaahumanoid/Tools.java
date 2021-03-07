package awele.bot.aaahumanoid;

import awele.bot.Bot;
import awele.bot.aaahumanoid.training.minmax9.MinMaxBot;
import awele.bot.aaahumanoid.training.random.RandomBot;
import awele.core.Awele;
import awele.core.InvalidBotException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Tools {

    private Humanoid bot1;
    private Humanoid bot2;

    ExplorationTree e1;
    ExplorationTree e2;


    //BOTS FOR TRAINING
    private final RandomBot random;
    private final MinMaxBot minMaxBot9;

    public int[] weightBase;
    public int[] bestWeight;


    public Tools() throws InvalidBotException {


        this.random = new RandomBot();
        this.minMaxBot9 = new MinMaxBot();

        this.weightBase = new int[8];
        this.weightBase[0] = 12;
        this.weightBase[1] = 12;
        this.weightBase[2] = 19;
        this.weightBase[3] = 10;
        this.weightBase[4] = 16;
        this.weightBase[5] = 2;
        this.weightBase[6] = 8;
        this.weightBase[7] = 10;

        this.bestWeight = weightBase;
    }


    protected int[] getBetterWeightFeatures(int time) {
        if (time == 0) return bestWeight;
        long start = System.currentTimeMillis();
        try {
            bot1 = new Humanoid();
            e1 = new ExplorationTree();
            e1.initialValueDelta = 1.15f;
            bot1.explorationTree = e1;

            bot2 = new Humanoid();
            e2 = new ExplorationTree();
            e2.initialValueDelta = 1.15f;
            bot2.explorationTree = e1;
        } catch (InvalidBotException e) {
            e.printStackTrace();
        }

        do {
            for (int j = 0; j < 10; j++) {
                int[] weight = compareWeightFeatures(bestWeight, mutateWeightFeatures(bestWeight.clone()));
                if (weight != bestWeight) bestWeight = weight;
                if (start - System.currentTimeMillis() < time) {
                    bestWeight = compareWeightFeatures(bestWeight, weightBase);
                    break;
                }

            }
            bestWeight = compareWeightFeatures(bestWeight, weightBase);
        } while (System.currentTimeMillis() - start < time);
        System.out.println("Mutation Ended : " + bestWeight);
        return bestWeight;
    }

    private int[] mutateWeightFeatures(int[] base) {
        int maxWeight = 24;
        int index = 2 + (int) (Math.random() * ((7 - 2) + 1));
        int newWeight = (int) (Math.random() * maxWeight);
        base[index] = newWeight;
        return base;
    }

    private int[] compareWeightFeatures(int[] weight1, int[] weight2) {
        int win1 = 0, win2 = 0;
        bot1.explorationTree.weightFeatures = weight1;
        bot2.explorationTree.weightFeatures = weight2;
        try {
            Awele awele = new Awele(bot1, bot2);
            for (int i = 0; i < 3; i++) {

                awele.play();

                if (awele.getWinner() == 0) win1++;
                else if (awele.getWinner() == 1) win2++;
            }
        } catch (InvalidBotException e) {
            e.printStackTrace();
        }

        if (win1 < win2) return weight2;
        return weight1;
    }

    private void playGame(Bot bot1, Bot bot2) {
        try {
            Awele awele = new Awele(bot1, bot2);
            awele.play();
        } catch (InvalidBotException e) {
            e.printStackTrace();
        }
    }

    protected void upgradeCategories(Humanoid humanoid) {
        playGame(random, humanoid);
        playGame(minMaxBot9, humanoid);
    }


    // CAN BE BETTER
    protected void initCategoriesWithData() {
        try {
            File f = new File("data/awele.data");
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine;
            b.readLine();
            while ((readLine = b.readLine()) != null) {


                String[] strings = readLine.split(",");
                int startIndex = Character.getNumericValue(strings[12].charAt(1)) - 1;
                int nStartSeed = Integer.parseInt(strings[startIndex]);

                int endIndex = startIndex;
                int nStartSeedTemp = nStartSeed;

                while (nStartSeedTemp > 0) {
                    if (endIndex == 11) endIndex = -1;
                    endIndex++;
                    if (startIndex == endIndex) {
                        continue;
                    }

                    nStartSeedTemp--;
                }

                int nEndSeeds = Integer.parseInt(strings[endIndex]);
                if (nStartSeed < 12) nEndSeeds += 1;
                else if (nStartSeed < 23) nEndSeeds += 2;
                else if (nStartSeed < 34) nEndSeeds += 3;
                else if (nStartSeed < 45) nEndSeeds += 4;
                else nEndSeeds += 5;

                int category = (startIndex + 6 * nStartSeed + 6 * 48 * nEndSeeds);
                if (strings[13].equals("G")) Humanoid.categories[category] = 1000;
                else Humanoid.categories[category] = -1000;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
