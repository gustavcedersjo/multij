package se.lth.cs.sovel.model.analysis;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import se.lth.cs.sovel.model.Definition;

public class DispatchOnGenerics implements Analysis {
	private final Types util;
	private final Messager messager;
	private static final HasGenerics hasGenerics = new HasGenerics();;
	
	public DispatchOnGenerics(ProcessingEnvironment processingEnv) {
		util = processingEnv.getTypeUtils();
		messager = processingEnv.getMessager();
	}


	@Override
	public boolean check(List<Definition> definitions) {
		Iterator<Definition> iter = definitions.iterator();
		if (iter.hasNext()) {
			List<TypeMirror> types = iter.next().getParamTypes();
			boolean[] same = new boolean[types.size()];
			Arrays.fill(same, true);
			while (iter.hasNext()) {
				Definition def = iter.next();
				for (int i = 0; i < same.length; i++) {
					if (same[i] && !util.isSameType(types.get(i), def.getParamTypes().get(i))) {
						same[i] = false;
					}
				}
			}
			
			boolean result = true;
			for (Definition def : definitions) {
				for (int i = 0; i < same.length; i++) {
					VariableElement par = def.getMethod().getParameters().get(i);
					if (!same[i] && hasGenerics(par.asType())) {
						result = false;
						messager.printMessage(Diagnostic.Kind.ERROR, "Can not do dynamic dispatch on generic type.", par);
					}
				}
			}
			return result;
		} else {
			return true;
		}
	}

	private boolean hasGenerics(TypeMirror typeMirror) {
		return typeMirror.accept(hasGenerics, null);
	}
	
	private static class HasGenerics extends SimpleTypeVisitor8<Boolean, Void> {

		public HasGenerics() {
			super(false);
		}
		
		@Override
		public Boolean visitDeclared(DeclaredType type, Void p) {
			return !type.getTypeArguments().isEmpty();
		}
		
		
		@Override
		public Boolean visitTypeVariable(TypeVariable type, Void p) {
			return true;
		}
	}
}
