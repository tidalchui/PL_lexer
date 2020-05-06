package lexer;

class WordItem{
	String key;//关键词
	Integer encode;//编码
	Integer line;//行号
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