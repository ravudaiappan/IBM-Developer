package com.makotojava.ncaabb.tui;

import org.apache.log4j.Logger;
import org.deeplearning4j.util.ModelSerializer;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class NetworkPersister {

  private static final Logger log = Logger.getLogger(NetworkPersister.class);

  private static final byte KEEP_LOOPING = -1;

  public static void persistNetworks(final Scanner scanner,
                                     final List<NetworkCandidate> networkCandidateList) {
    byte networkNumber = KEEP_LOOPING;
    while (networkNumber == KEEP_LOOPING && !networkCandidateList.isEmpty()) {
      System.out.println("Enter the number of the network you want to persist (enter 0 to quit):");
      int index = 0;
      for (NetworkCandidate networkCandidate : networkCandidateList) {
        NetworkParameters networkParameters = networkCandidate.getNetworkParameters();
        System.out.printf("%d - (Trained: %s) %48s%n",
          index + 1,
          networkParameters.getWhenTrained().format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")),
          networkParameters.getNetworkLayout());
        index++;
      }
      if (scanner.hasNextByte()) {
        networkNumber = scanner.nextByte();
        if (networkNumber < 0 || networkNumber > networkCandidateList.size()) {
          networkNumber = KEEP_LOOPING;
          continue;
        }
        if (networkNumber != 0) {
          networkNumber = saveSelectedNetworkOrNot(scanner, networkCandidateList, networkNumber);
        } else {
          break;
        }
      } else {
        System.out.printf("%s is not a valid choice.%n", scanner.next());
        networkNumber = KEEP_LOOPING;
      }
    }
  }

  private static byte saveSelectedNetworkOrNot(final Scanner scanner,
                                          final List<NetworkCandidate> networkCandidateList,
                                          final byte networkNumber) {
    byte ret = -1;
    String yesOrNo = null;
    while (yesOrNo == null) {
      System.out.printf("Save network %s (y/n)?%n", networkCandidateList.get(networkNumber - 1).getNetworkParameters().getNetworkLayout());
      yesOrNo = scanner.next().trim();
      if (yesOrNo.equalsIgnoreCase("y")) {
        NetworkCandidate networkCandidate = networkCandidateList.get(networkNumber - 1);
        if (saveNetwork(networkCandidate)) {
          networkCandidateList.remove(networkCandidate);
        }
      } else if (yesOrNo.equalsIgnoreCase("n")) {
        break;
      } else {
        yesOrNo = null; // Keep looping until we get something we understand
      }
    }
    return ret;
  }

  /**
   * Saves the specified network and returns true if successful, false otherwise.
   */
  private static boolean saveNetwork(final NetworkCandidate networkCandidate) {
    boolean ret = false;
    System.out.printf("Saving network: %s...%n", networkCandidate.getNetworkParameters().getNetworkLayout());
    String networkFileName = String.format("NcaaBbNet-%s.zip", networkCandidate.getNetworkParameters().getNetworkLayout());
    try {
      ModelSerializer.writeModel(networkCandidate.getMultiLayerNetwork(), networkFileName, true);
      ret = true;
    } catch (IOException e) {
      String message = String.format("Error saving network file '%s': %s", networkFileName, e.getLocalizedMessage());
      log.error(message, e);
    }
    return ret;
  }
}