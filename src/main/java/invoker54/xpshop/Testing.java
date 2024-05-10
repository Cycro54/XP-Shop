package invoker54.xpshop;

import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.event.generation.ShopGenerationEvent;

public class Testing {
    public static ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);

    public static void test() {
//        Set<Double> testList = new HashSet<>(List.of(
//                2D,
//                2D,
//                13D,
//                14D,
//                16D,
//                18D,
//                20D,
//                28D,
//                29D,
//                30D,
//                33D,
//                42D,
//                45D,
//                46D,
//                70D,
//                91D,
//                108D,
//                166D,
//                185D,
//                206D,
//                242D,
//                294D,
//                302D,
//                331D,
//                346D,
//                378D,
//                408D,
//                607D,
//                744D,
//                1895D,
//                2051D,
//                2942D,
//                3368D,
//                7063D,
//                7954D,
//                9350D,
//                13368D,
//                51937D
//        ));
////        int count = testList.size();
////        double highestNumber = testList.get(testList.size()-1);
////        double lowestNumber = testList.get(0);
////        double averageNumber = 0;
////        for(double number : testList) averageNumber += number;
////        averageNumber /= count;
////        List<String> resultList = new ArrayList<>();
////
////        //Test 1: increment
////        for (Double testNumber : testList){
////            double sellPrice = 2 * (resultList.size() + 1);
////            sellPrice = Math.min(sellPrice, testNumber * XPShopConfig.salePercentage);
////            resultList.add(testNumber+":"+sellPrice);
////        }
////        LOGGER.warn("Test 1: Increment");
////        LOGGER.error(resultList.toString());
////        resultList.clear();
////
////        //Test 2: Closer to the highest price item, higher the sell price.
////        for (Double testNumber : testList){
////            double sellPrice = testList.get(testList.size() - 1) * 0.25F;
////            sellPrice = sellPrice/testList.size();
////            sellPrice = sellPrice * (resultList.size() + 1);
////            sellPrice = Math.min(sellPrice, testNumber * XPShopConfig.salePercentage);
////            resultList.add(testNumber+":"+sellPrice);
////        }
////        LOGGER.warn("Test 2: Highest price");
////        LOGGER.error(resultList.toString());
////        resultList.clear();
////
////        //Test 3: Average price as highest instead.
////        for (Double testNumber : testList){
////            double sellPrice = averageNumber * 0.25F;
////            sellPrice = sellPrice/testList.size();
////            sellPrice = sellPrice * (resultList.size() + 1);
////            sellPrice = Math.min(sellPrice, testNumber * XPShopConfig.salePercentage);
////            resultList.add(testNumber+":"+sellPrice);
////        }
////        LOGGER.warn("Test 3: Average price as highest instead.");
////        LOGGER.error(resultList.toString());
////        resultList.clear();
////
////        //Test 4: Average price by increment
////        double sum = 0;
////        for (Double testNumber : testList){
////            sum += testNumber;
////            double sellPrice = sum/(resultList.size()+1);
////            sellPrice *= XPShopConfig.salePercentage;
////            resultList.add(testNumber+":"+sellPrice);
////        }
////        LOGGER.warn("Test 4: Average price by increment");
////        LOGGER.error(resultList.toString());
////        resultList.clear();
////
////        //Test 5: Average price by increment and full price
////        sum = 0;
////        for (Double testNumber : testList){
////            sum += testNumber;
////            double usualSalPrice = testNumber * 0.25F;
////            double sellPrice = sum/(resultList.size()+1);
////            sellPrice *= XPShopConfig.salePercentage;
////            sellPrice = (usualSalPrice + sellPrice)/2F;
////            resultList.add(testNumber+":"+sellPrice);
////        }
////        LOGGER.warn("Test 5: Average price by increment and full price");
////        LOGGER.error(resultList.toString());
////        resultList.clear();
////
////        //Test 6: Average price as highest instead.
////        for (Double testNumber : testList){
////            double sellPrice = averageNumber * 0.25F;
////            sellPrice = (sellPrice + (testNumber * 0.25F))/2F;
////            sellPrice = Math.min(sellPrice, testNumber * XPShopConfig.salePercentage);
////            resultList.add(testNumber+":"+sellPrice);
////        }
////        LOGGER.warn("Test 6: Average price sale percentage with current number sale percentage");
////        LOGGER.error(resultList.toString());
////        resultList.clear();
//        ModStopWatch singleThread = ModStopWatch.getTimer(XPShop.MOD_ID, "singleTest", ModStopWatch.Time.ACCUMULATE);
//        MiniTicker<?> singleTicker = singleThread.ticker();
//        ModStopWatch multiThread = ModStopWatch.getTimer(XPShop.MOD_ID, "multiTest", ModStopWatch.Time.ACCUMULATE);
//        MiniTicker<?> multiTicker = multiThread.ticker();
//
//        singleTicker.reset();
//        for (double x : testList) {
//            for (int a = 0; a < 1000000; a++) {
//                final AtomicDouble finalX = new AtomicDouble(x);
//                finalX.set(divideOperation(finalX.get()));
//                finalX.set(multiplyOperation(finalX.get()));
//                finalX.set(finalizeProduct(finalX.get()));
//            }
//        }
//        singleTicker.record("");
//        LOGGER.info(singleThread.compileTime("Single Thread average time", false, true));
//
////        AtomicInteger atomInt = new AtomicInteger(0);
//        multiTicker.reset();
//        testList.parallelStream().forEach((x) -> {
//            for (int a = 0; a < 1000000; a++) {
//                final AtomicDouble finalX = new AtomicDouble(x);
//                finalX.set(divideOperation(finalX.get()));
//                finalX.set(multiplyOperation(finalX.get()));
//                finalX.set(finalizeProduct(finalX.get()));
//            }
//        });
//        multiTicker.record("");
//        LOGGER.info(multiThread.compileTime("Multi Thread average time", false, true));

    }

    public static double divideOperation(double x) {
        x /= 12D;
        x /= 0.5D;
        return x;
    }

    public static double multiplyOperation(double x) {
        x *= 6D;
        x *= 0.9321D;
        return x;
    }

    public static double finalizeProduct(double x) {
        x += 23;
        x -= 2;
        x = Double.parseDouble(ShopGenerationEvent.df.format(x));
        return x;
    }
}
