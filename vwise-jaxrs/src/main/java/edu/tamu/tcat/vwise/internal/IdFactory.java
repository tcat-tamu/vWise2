package edu.tamu.tcat.vwise.internal;

import static java.text.MessageFormat.format;

import java.util.Comparator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class IdFactory implements Supplier<String>, Comparator<String>
{
   private final static Logger logger = Logger.getLogger(IdFactory.class.getName());

   public final static String PROP_ALPHABET = "ids.{0}.obfuscate.alphabet";
   public final static String PROP_BLOCK_SIZE = "ids.{0}.obfuscate.block_size";
   public final static String PROP_MIN_LENGTH = "ids.{0}.obfuscate.min_length";

   private IdObfuscator obfuscator;
   private AtomicLong counter = new AtomicLong(1);

   public IdFactory(Properties props, String type)
   {
      obfuscator = initId(props, type);
   }

   private IdObfuscator initId(Properties props, String key)
   {
      String INFO_OBFUSCATOR_CFG = "Initializing id obfuscator for {3}:"
            + "\n\tAlphabet: {0}"
            + "\n\tBlock Size: {1}"
            + "\n\tMin Length: {2}";

      String alphabet = props.getProperty(format(PROP_ALPHABET, key), IdObfuscator.ALPHABET);
      int blockSize = getIntProperty(props, format(PROP_BLOCK_SIZE, key), IdObfuscator.BLOCK_SIZE);
      int minLength = getIntProperty(props, format(PROP_MIN_LENGTH, key), IdObfuscator.MIN_LENGTH);

      logger.finer(() -> format(INFO_OBFUSCATOR_CFG, alphabet, Integer.valueOf(blockSize), Integer.valueOf(minLength), key));
      return new IdObfuscator(alphabet, blockSize, minLength);
   }

   private int getIntProperty(Properties props, String key, int defaultValue)
   {
      String property = props.getProperty(key, String.valueOf(defaultValue));
      try
      {
         return Integer.parseInt(property);
      } catch (NumberFormatException ex) {
         throw new IllegalArgumentException(format("The value {0} for property {1} must be an integer"));
      }
   }

   @Override
   public String get()
   {
      long id = counter.getAndIncrement();
      return obfuscator.encode(id);
   }

   @Override
   public int compare(String idA, String idB)
   {
      long a = obfuscator.decode(idA);
      long b = obfuscator.decode(idB);

      return Long.compare(a, b);
   }

}