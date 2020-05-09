package lexer;

public class WordItem{
	public String key;//¹Ø¼ü´Ê
	public Integer encode;//±àÂë
	public Integer line;//ĞĞºÅ
	public WordItem(String key, Integer encode, Integer line){
		this.key = key;
		this.encode = encode;
		this.line = line;
	}
    @Override
	public String toString() {
    	if(encode<131)
    		return "<"+key+">";
    	else
    		return "<"+SymbolTable.getSortCodeByEncode(encode).symbol+","+key+">";
	}
}