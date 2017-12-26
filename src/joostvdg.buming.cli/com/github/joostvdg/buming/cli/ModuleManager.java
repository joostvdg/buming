package com.github.joostvdg.buming.cli;

public class ModuleManager {

    public void printModulesInBootLayer(){
        System.out.println("All modules in Boot Layer:");
        ModuleLayer.boot().modules().forEach(System.out::println);

        ModuleLayer.boot().modules().
            forEach(module -> printModuleInfo(module));
    }

    private void printModuleInfo(Module module) {
        StringBuilder moduleInfo = new StringBuilder();
        moduleInfo.append("----------------\n");
        moduleInfo.append("Module: ");
        moduleInfo.append(module.getName());
        moduleInfo.append("\nPackages: ");
        module.getPackages().forEach(s -> moduleInfo.append(packageInfo(module,s)));
        moduleInfo.append("\n----------------");
        System.out.println(moduleInfo.toString());
    }

    private String packageInfo(Module module, String packageName) {
        if (module.getName().startsWith("java.")) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("\n Package: ");
        builder.append(packageName);
        if (module.isOpen(packageName)) {
            builder.append(", open");
        }
        if (module.isExported(packageName)) {
            builder.append(", exported");
        }

        return builder.toString();
    }

    public void printModulesInLayer(ModuleLayer moduleLayer) {
        System.out.println("Printing info for modulelayer: " + moduleLayer.toString() );
        moduleLayer.parents().forEach(parent -> System.out.println(" > Parent: " + parent.configuration().toString()));
        System.out.println("All modules in Layer: ");
        moduleLayer.modules().forEach(System.out::println);
    }
}
