package com.emistoolbox.server.renderer.pdfreport.layout;

public interface LayoutVisitor<T>
{
	public T visit(LayoutPage page); 
	public T visit(LayoutFrame frame);
}
