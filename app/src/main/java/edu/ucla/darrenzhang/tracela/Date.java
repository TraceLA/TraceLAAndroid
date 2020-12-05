package edu.ucla.darrenzhang.tracela;

public class Date implements Comparable {
    private int month;
    private int day;
    private int year;
    private int hour;
    private int minute;
    private int second;
    private int [] comparingArray;

    public Date(String timestamp){
        parse(timestamp);
        comparingArray = new int[]{year, month, day, hour, minute, second};
    }

    public void parse(String s){
        if (s.length() >=4) year = Integer.valueOf(s.substring(0,4));
        if (s.length() >=7) month = Integer.valueOf(s.substring(5,7));
        if (s.length() >=10) day = Integer.valueOf(s.substring(8,10));
        if (s.length() >=13) hour = Integer.valueOf(s.substring(11, 13));
        if (s.length() >=16) minute = Integer.valueOf(s.substring(14, 16));
        if (s.length() >=19) second = Integer.valueOf(s.substring(17, 19));

    }

    @Override
    public int compareTo(Object o) {
        Date other = (Date)o;
        int [] otherArr = other.getComparingArray();
        for (int i = 0; i<comparingArray.length && i < otherArr.length; i++){
            if (comparingArray[i]>otherArr[i]){
                return 1;
            } else if (comparingArray[i] < otherArr[i]){
                return -1;
            }
        }
        return 0;
    }

    public String toString(){
        return month+"-"+day+"-"+year+" "+hour+":"+minute+":"+second;
    }
    public int[] getComparingArray() {
        return comparingArray;
    }

    public void setComparingArray(int[] comparingArray) {
        this.comparingArray = comparingArray;
    }
    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
