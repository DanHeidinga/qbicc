package org.qbicc.graph;

import java.util.Objects;

import org.qbicc.type.definition.element.ExecutableElement;

/**
 * An operation which may throw an exception.
 */
public final class Try extends AbstractTerminator implements Resume {
    private final Triable delegateOperation;
    private final BlockLabel resumeTargetLabel;
    private final BlockLabel exceptionHandler;
    private final BasicBlock terminatedBlock;

    Try(final Node callSite, final ExecutableElement element, final Triable delegateOperation, final BlockEntry blockEntry, final BlockLabel resumeTargetLabel, final BlockLabel exceptionHandler) {
        super(callSite, element, delegateOperation.getSourceLine(), delegateOperation.getBytecodeIndex());
        terminatedBlock = new BasicBlock(blockEntry, this);
        this.delegateOperation = delegateOperation;
        this.resumeTargetLabel = resumeTargetLabel;
        this.exceptionHandler = exceptionHandler;
    }

    public BasicBlock getTerminatedBlock() {
        return terminatedBlock;
    }

    public Triable getDelegateOperation() {
        return delegateOperation;
    }

    public BlockLabel getExceptionHandlerLabel() {
        return exceptionHandler;
    }

    public BasicBlock getExceptionHandlerBranch() { return BlockLabel.getTargetOf(exceptionHandler); }

    public BasicBlock getExceptionHandler() {
        return BlockLabel.getTargetOf(exceptionHandler);
    }

    public BlockLabel getResumeTargetLabel() {
        return resumeTargetLabel;
    }

    @Override
    public Node getDependency() {
        return delegateOperation;
    }

    public BasicBlock getResumeBranch() { return BlockLabel.getTargetOf(resumeTargetLabel); }

    public int getSuccessorCount() {
        return 2;
    }

    public BasicBlock getSuccessor(final int index) {
        return index == 0 ? BlockLabel.getTargetOf(resumeTargetLabel) : index == 1 ? getExceptionHandler() : Util.throwIndexOutOfBounds(index);
    }

    public <T, R> R accept(final TerminatorVisitor<T, R> visitor, final T param) {
        return visitor.visit(param, this);
    }

    int calcHashCode() {
        return Objects.hash(Try.class, delegateOperation, resumeTargetLabel, getExceptionHandlerLabel());
    }

    public boolean equals(final Object other) {
        return other instanceof Try && equals((Try) other);
    }

    public boolean equals(final Try other) {
        return this == other || other != null
            && delegateOperation.equals(other.delegateOperation)
            && resumeTargetLabel.equals(other.resumeTargetLabel)
            && getExceptionHandlerLabel().equals(other.getExceptionHandlerLabel());
    }
}
