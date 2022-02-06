/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package templateserver;

/**
 *
 * @author Danie
 */
public class gamePlay2 {
    private String name;
    private int score;
    public gamePlay2(String n1, int n2){
        name = n1;
        score = n2;
    }
    public String nameShow(){
        return name;
    }
    public int scoreShow(){
        return score;
    }
    public void increaseScore(int n1){
        score = n1;
    }
    public void changer(String n1, int n2){
        name = n1;
        score = n2;
    }
}
