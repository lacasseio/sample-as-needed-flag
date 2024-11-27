package nokeesamples;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.nativeplatform.plugins.NativeComponentModelPlugin;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.toolchain.GccCompatibleToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;

import javax.inject.Inject;
import java.util.Collections;
import java.util.ListIterator;

public abstract class AsNeededLinkOptions {
    private static final String AS_NEEDED_ENABLED = "--nokee-as-needed";
    private static final String AS_NEEDED_DISABLED = "--nokee-no-as-needed";
    private final Property<State> state;

    @Inject
    public AsNeededLinkOptions(ObjectFactory objects) {
        this.state = objects.property(State.class).convention(State.DEFAULT);
    }

    public AsNeededLinkOptions enable() {
        state.set(State.ENABLED);
        return this;
    }

    public AsNeededLinkOptions disable() {
        state.set(State.DISABLED);
        return this;
    }

    public Provider<Iterable<String>> asProvider() {
        return state.map(it -> {
            switch (it) {
                case DEFAULT: return Collections.emptyList();
                case ENABLED: return Collections.singleton(AS_NEEDED_ENABLED);
                case DISABLED: return Collections.singleton(AS_NEEDED_DISABLED);
            }
            // Unknown, just ignore
            return Collections.emptyList();
        });
    }

    private enum State {
        DEFAULT, ENABLED, DISABLED
    }

    /*private*/ abstract static /*final*/ class Rule implements Plugin<Project> {
        @Inject
        public Rule() {}

        @Override
        public void apply(Project project) {
            project.getPlugins().withType(NativeComponentModelPlugin.class).configureEach(__ -> {
                project.getTasks().withType(AbstractLinkTask.class).configureEach(task -> {
                    final AsNeededLinkOptions options = task.getExtensions().create("asNeeded", AsNeededLinkOptions.class);
                    task.getLinkerArgs().addAll(options.asProvider());
                });

                project.getExtensions().configure(NativeToolChainRegistry.class, toolChains -> {
                    toolChains.withType(GccCompatibleToolChain.class).configureEach(toolChain -> {
                        toolChain.eachPlatform(platform -> {
                            platform.getLinker().withArguments(args -> {
                                if (args.remove(AS_NEEDED_ENABLED)) {
                                    ListIterator<String> iter = args.listIterator();
                                    while (iter.hasNext()) {
                                        String arg = iter.next();
                                        if (arg.endsWith(".so") || arg.endsWith(".dylib")) {
                                            iter.previous();
                                            iter.add("-Wl,--as-needed");
                                            break;
                                        }
                                    }
                                } else if (args.remove(AS_NEEDED_DISABLED)) {
                                    ListIterator<String> iter = args.listIterator();
                                    while (iter.hasNext()) {
                                        String arg = iter.next();
                                        if (arg.endsWith(".so") || arg.endsWith(".dylib")) {
                                            iter.previous();
                                            iter.add("-Wl,--no-as-needed");
                                            break;
                                        }
                                    }
                                }
                            });
                        });
                    });
                });
            });
        }
    }
}
