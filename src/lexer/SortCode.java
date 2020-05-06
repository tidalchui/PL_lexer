package lexer;

class SortCode{
	final String word;//单词
	final Integer encode;//编码
	final String symbol;//注记符
	public SortCode(String word, Integer encode, String symbol){
		this.word = word;
		this.encode = encode;
		this.symbol = symbol;
	}
}