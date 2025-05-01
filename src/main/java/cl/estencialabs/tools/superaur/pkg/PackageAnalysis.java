package cl.estencialabs.tools.superaur.pkg;

import cl.estencialabs.tools.superaur.model.Package;
import cl.estencialabs.tools.superaur.util.CollectionUtil;
import lombok.Data;

import java.util.List;

@Data
public class PackageAnalysis {
    private final Package mainPkg;
    private final List<Package> listAnalyzed;

    public PackageAnalysis(String mainPkg) {
        this.mainPkg = new Package(mainPkg);
        this.listAnalyzed = CollectionUtil.newFastList();
    }

    public synchronized void addPackage(Package pkg) {
        listAnalyzed.add(pkg);
    }

    public synchronized Package getPkg(String pkgName) {
        return !listAnalyzed.isEmpty() ? listAnalyzed.parallelStream()
                .filter(pkg -> pkg.getName().equals(pkgName))
                .findFirst().orElse(null) : null;
    }

    public synchronized Package getPkgClone(String pkgName) {
        Package pkg = getPkg(pkgName);
        try {
            return pkg != null ? pkg.clone() : null;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
