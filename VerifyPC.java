import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;

public class VerifyPC {

  private static final long RAM_MIN_GO = 8;
  private static final long RAM_RECO_GO = 16;
  private static final long DISQUE_MIN_GO = 8;
  private static final long DISQUE_RECO_GO = 30;
  private static final int COEURS_RECO = 4;

  private static final long MO = 1024L * 1024;
  private static final long GO = 1024L * MO;

  void main() {
    IO.println("Bonjour");
    verifierRAM();
    verifierCPU();
    verifierDisque();

  }

  void verifierRAM() {
    IO.println("1) Mémoire vive (RAM)");
    var os  = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    long totale = os.getTotalMemorySize();
    long libre = os.getFreeMemorySize();
    long totaleGo = Math.round((double) totale / GO);
    IO.println("  RAM totale : " + totale / MO + " Mo  (~" + totaleGo + " Go)");
    IO.println("  RAM libre  : " + libre / MO + " Mo  (en ce moment)");

    if (totaleGo >= RAM_RECO_GO) {
      ok("RAM suffisante pour Android Studio + emulateur");
    } else if (totaleGo >= RAM_MIN_GO) {
      moyen("Studio tournera, mais l'emulateur sera lent. Prefere un vrai telephone en USB.");
    } else {
      ko("Moins de " + RAM_MIN_GO + " Go : Android Studio sera tres penible.");
    }
  }

  void verifierCPU() {
    IO.println("2) Processeur (CPU)");
    int coeurs = Runtime.getRuntime().availableProcessors();
    IO.println("  Coeurs : " + coeurs);

    if (coeurs >= COEURS_RECO) ok("Assez de coeurs");
    else moyen("Peu de coeurs : compilation lentes");
    Path cpuinfo = Path.of("/proc/cpuinfo");
    if (Files.exists(cpuinfo)) {
      try {
        String texte = Files.readString(cpuinfo);
        if (texte.contains(" vmx") || texte.contains(" svm")) {
          ok("Virtualisation materielle activee");
        } else {
          ko("Virtualisation non detectee : active VT-x / AMD-V dans le BIOS");
        }
      } catch (Exception e) {
        IO.println("  Impossible de lire /proc/cpuinfo : " + e.getMessage());
      }
    } else {
      IO.println("  (Hors Linux : verifie la virtualisation a la main)");
    }
  }

  void verifierDisque() {
    IO.println("3) Disque");
    var maison = new File(System.getProperty("user.home"));
    long libreGo = maison.getUsableSpace() / GO;
    IO.println("  Espace libre : " + libreGo + " Go");
    if (libreGo >= DISQUE_RECO_GO) ok("Espace confortable");
        else if (libreGo >= DISQUE_MIN_GO) moyen("Suffisant, mais le SDK grossit vite");
        else ko("Moins de " + DISQUE_MIN_GO + " Go libres : fais de la place");
  }

  void ok(String m)    { IO.println("  [OK]     " + m); }
  void moyen(String m) { IO.println("  [JUSTE]  " + m); }
  void ko(String m)    { IO.println("  [KO]     " + m); }

}
