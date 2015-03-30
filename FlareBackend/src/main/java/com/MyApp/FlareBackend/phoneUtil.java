package com.MyApp.FlareBackend;

public class phoneUtil {
    public static String[] splitPhoneNumber(String number) {

        String digits = "";
        for(Character c : number.toCharArray()) {
            if(Character.isDigit(c))
                digits += c;
        }

        String phone = "";
        String code = "";

        for (int i= digits.length()-1 ; i >= 0; i--) {
            if(phone.length() < 10) {
                phone += digits.charAt(i);
            } else {
                code += digits.charAt(i);
            }
        }

        return new String[]{new StringBuilder(code).reverse().toString(),new StringBuilder(phone).reverse().toString()};
    }
}
