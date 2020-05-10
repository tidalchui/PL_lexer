package lexer;
import java.util.ArrayList;

class ErrorType {
	String type;
	Integer line;
	String key;
	public ErrorType(String t , Integer l , String k) {
		// TODO �Զ����ɵĹ��캯�����
		type=t;line=l;key=k;
	}
	@Override
	public String toString() {
		return type+" line:"+line+" key:"+key;
	}
}


public class ErrorWriter {


	public static ArrayList<ErrorType> errorList = new ArrayList<>();
	public static void addError( String t , Integer l , String k ) {
		errorList.add(new ErrorType(t, l, k));
	}
	public static String getErrorString(int i) {
		if(i > errorList.size())
			return "����Խ��";
		else {
			ErrorType eType = errorList.get(i);
			return "line:"+eType.line+ " "+eType.type+" key:"+eType.key;
		}
	}

}
