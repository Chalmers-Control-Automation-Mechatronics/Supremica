package net.sourceforge.waters.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;

public class EditorGuardActionBlock extends EditorLabelGroup
	implements MouseListener{

	private static final int LEFTMARGIN = 2;
	private GuardActionBlockSubject mGuardActionBlock;
	private String mGuardExpression;
	private ArrayList<JComponent> mGuardActionExpressionLabels;
	private int mGuardLabelIndex;
	private int mActionLabelIndex;
	private Component[] mPanelContent;
	private JLabel mCollapsedContent;
	private boolean isCollapsed;
	private boolean mIsHighlighted = false;
	private boolean mHasGuard = false;

	public EditorGuardActionBlock(EditorEdge parentEdge, EditorSurface editorSurface) {
		super(parentEdge, editorSurface);
		// This is a GuardActionBlock
		type = GUARDACTIONBLOCK;
		Font defaultFont = new Font(null);
		panel.removeAll();
		isCollapsed = false;
		mCollapsedContent = new JLabel(" G/A");
		mCollapsedContent.setForeground(EditorColor.GUARDACTIONHEADER);
		mPanelContent = new Component[0];
		List<BinaryExpressionProxy> actions;
		mGuardActionExpressionLabels = new ArrayList<JComponent>();
		mGuardActionBlock = parentEdge.getSubject().getGuardActionBlock();
		parent.getParent().addMouseListener(this);
		if(mGuardActionBlock.getGuard() != null || !mGuardActionBlock.getActionList().isEmpty()) {
			
			//Add guard header to guard action block
			JTextField header = new JTextField(" Guard:");
			header.addMouseListener(this);
			header.setEditable(false);
			addToPanel(header, 0);
			mGuardActionExpressionLabels.get(0).setFont(new Font(null, Font.PLAIN, defaultFont.getSize()));
			mGuardActionExpressionLabels.get(0).setForeground(EditorColor.GUARDACTIONHEADER);
			
			//Add action header to guard action block
			header = new JTextField(" Action:");
			header.addMouseListener(this);
			header.setEditable(false);
			addToPanel(header, 1);
			mGuardActionExpressionLabels.get(1).setFont(new Font(null, Font.PLAIN, defaultFont.getSize()));
			mGuardActionExpressionLabels.get(1).setForeground(EditorColor.GUARDACTIONHEADER);
			
			mGuardLabelIndex = 0;
			mActionLabelIndex = 1;
			
			//Add guard and action
			mGuardExpression = mGuardActionBlock.getGuard();
			actions = mGuardActionBlock.getActionList();
			this.addGuard(mGuardExpression);
			for(BinaryExpressionProxy action: actions) {
				this.addAction(action);
			}
		}
		this.shadow = this.getParent().getEditorLabelGroup().shadow;
	}
	
	private void addAction(BinaryExpressionProxy action) {
		EditorAction editorAction = new EditorAction(action, this);
		addToPanel(editorAction, mActionLabelIndex + 1);
		
		resizePanel();
	}
	private void addGuard(String guard)
	{
		EditorGuard editorGuard = new EditorGuard(guard, this);
		addToPanel(editorGuard, mGuardLabelIndex + 1);
		resizePanel();
		mActionLabelIndex += 1;
		mHasGuard = true;
	}
	
	private void addToPanel(JTextField expressionLabel, int index)
	{
		expressionLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
		expressionLabel.setOpaque(false);
		
		if(isCollapsed) {
			expandPanel(); 
		}
		mGuardActionExpressionLabels.add(expressionLabel);
		panel.add(expressionLabel, index);
	}
	
	public void setPanelLocation()
	{
		int x = getOffsetX() + (int) parent.getTPointX();
		int y = getOffsetY() + (int) parent.getTPointY() +
			parent.getEditorLabelGroup().getHeight() +
			2*EditorSurface.panelMarginAdjust;

		panel.setLocation(x, y);
	}

	public GuardActionBlockSubject getGuardActionBlock() {
		return mGuardActionBlock;
	}
	
	public boolean isHighlighted() {
		//The event label group owns the highlight property (in case there is a label group)
		if(parent != null) {
		return this.getParent().getEditorLabelGroup().isHighlighted();
		} else {
			return mIsHighlighted;
		}
	}
	
	public void setHighlighted(boolean isHighlighted) {
		//The event label group owns the highlight property (in case there is a label group)
		if(parent != null) {
		this.getParent().getEditorLabelGroup().
		setHighlighted(isHighlighted);
		} else {
			mIsHighlighted = isHighlighted;
		}
	}

	private void collapsePanel() {
		mPanelContent = panel.getComponents();
		panel.removeAll();
		panel.add(mCollapsedContent);
		resizePanel();
		parent.getParent().repaint();
		isCollapsed = true;
	}

	private void expandPanel() {
			panel.removeAll();
			for(Component component: mPanelContent) {
				panel.add(component);
			}
			resizePanel();
			parent.getParent().repaint();
			isCollapsed = false;
	}

	private void togglePanelCollapse() {
		if(isCollapsed) {
			expandPanel();
		} else {
			collapsePanel();
		}
	}

	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if((e.getComponent().getParent() == this.panel ||
				wasClicked(x,y)) && e.getClickCount() == 2) {
			togglePanelCollapse();
		}
	}

	public void mousePressed(MouseEvent arg0) {
		//do nothing
	}

	public void mouseReleased(MouseEvent arg0) {
		//do nothing
	}

	public void mouseEntered(MouseEvent arg0) {
		//do nothing
	}

	public void mouseExited(MouseEvent arg0) {
		//do nothing
	}

	public void remove(EditorAction action) {
		List<BinaryExpressionProxy> actionList = 
			mGuardActionBlock.getActionList();
		actionList.remove(action.getActionExpression());
		panel.remove(action);
	}

	public void remove(EditorGuard guard) {
		mHasGuard = false;
		mGuardActionBlock.setGuard(null);
		panel.remove(guard);
		mActionLabelIndex = 1;
	}
	public void modelChanged(ModelChangeEvent e)
	{
		//Do nothing. Overrides modelChanged of EditorLabelGroup
	}

	public boolean hasGuard() {
		return mHasGuard ;
	}
}
