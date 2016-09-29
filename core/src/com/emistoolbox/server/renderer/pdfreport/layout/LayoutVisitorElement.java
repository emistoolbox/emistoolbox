package com.emistoolbox.server.renderer.pdfreport.layout;

public interface LayoutVisitorElement 
{
	public <T> T accept(LayoutVisitor<T> visitor); 
}
