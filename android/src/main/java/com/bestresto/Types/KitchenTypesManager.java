package com.bestresto.Types;

import android.database.Cursor;

import com.bestresto.Database.DatabaseContract;
import com.bestresto.Database.DatabaseWork;
import com.bestresto.Database.QueryConditions;
import com.bestresto.ManagerInterface;

import java.util.ArrayList;
import java.util.HashMap;

public class KitchenTypesManager implements ManagerInterface {

    private int currentPrimeNumber = 0;

    @Override
    public void addAllDb(ArrayList<HashMap<String, Object>> data){
        DatabaseWork.cleanTable(DatabaseContract.KitchenTypesColumns.TABLE_NAME);
        StringBuilder SQLScript = new StringBuilder("INSERT INTO " + DatabaseContract.KitchenTypesColumns.TABLE_NAME + " (" +
                DatabaseContract.KitchenTypesColumns.INDEXID + ", " +
                DatabaseContract.KitchenTypesColumns.CAPTION + ", " +
                DatabaseContract.KitchenTypesColumns.SORT + ", " +
                DatabaseContract.KitchenTypesColumns.ACTIVE + ", " +
                DatabaseContract.KitchenTypesColumns.PRIMEID + ") VALUES ");
        ArrayList<Integer> primeNumber = generatePrimeNumber();
        int i = 0;
        for (HashMap<String, Object> type: data) {
            currentPrimeNumber = primeNumber.get(i++);
            SQLScript.append(createStringForSQLScript(type));
            if (type.equals(data.get(data.size() - 1))) {
                SQLScript.append(";");
            } else {
                SQLScript.append(", ");
            }
        }
        DatabaseWork.execSQL(SQLScript.toString());
    }

    @Override
    public String createStringForSQLScript(HashMap<String, Object> type) {
        String result = "(";
        result += (type.get(DatabaseContract.KitchenTypesColumns.INDEXID) == null ? 0 :
                Integer.parseInt(type.get(DatabaseContract.KitchenTypesColumns.INDEXID).toString())) + ", ";
        result += DatabaseWork.prepare(type.get(DatabaseContract.KitchenTypesColumns.CAPTION) == null ? "" :
                type.get(DatabaseContract.KitchenTypesColumns.CAPTION).toString()) + ", ";
        result += (type.get(DatabaseContract.KitchenTypesColumns.SORT) == null ? 0 :
                Integer.parseInt(type.get(DatabaseContract.KitchenTypesColumns.SORT).toString())) + ", ";
        result += (type.get(DatabaseContract.KitchenTypesColumns.ACTIVE) == null ? 0 :
                Integer.parseInt(type.get(DatabaseContract.KitchenTypesColumns.ACTIVE).toString())) + ", ";
        result += currentPrimeNumber;
        result += ")";
        return result;
    }

    @Override
    public Object getValue(Cursor cursor, String column, int index) {
        switch (column) {
            case DatabaseContract.KitchenTypesColumns.CAPTION:
                return cursor.getString(index);
            default:
                return cursor.getInt(index);
        }
    }

    private ArrayList<Integer> generatePrimeNumber(){
        int upNumber = 10000;
        ArrayList<Integer> result = new ArrayList<Integer>();
        boolean[] used = new boolean[upNumber];
        for (int i = 2; i < upNumber; ++i){
            if (!used[i]) {
                result.add(i);
                for (int j = i; j < upNumber; j += i) {
                    used[j] = true;
                }
            }
        }
        return result;
    }

    private ArrayList<Integer> stringToArray(String str){
        ArrayList<Integer> result = new ArrayList<>();
        String cur = "";
        for (int i = 0; i < str.length(); ++i){
            if (str.charAt(i) >= '0' && str.charAt(i) <= '9'){
                cur += str.charAt(i);
            }
            else{
                if (!cur.equals("")){
                    int num = Integer.parseInt(cur);
                    result.add(num);
                    cur = "";
                }
            }
        }
        if (!cur.equals("")){
            int num = Integer.parseInt(cur);
            result.add(num);
        }
        return result;
    }

    private ArrayList<Integer> prime(int num){
        ArrayList<Integer> result = new ArrayList<>();
        int up = num;
        for (int i = 2; i * i <= up; ++i){
            if (num % i == 0){
                result.add(i);
                while (num % i == 0)
                    num /= i;
            }
        }
        if (num != 1)
            result.add(num);
        return result;
    }

    public int getKitchenNumber(String req){
        ArrayList<Integer> kitchensId = stringToArray(req);
        int result = 1;

        QueryConditions queryConditions = new QueryConditions();
        queryConditions.setTableName(DatabaseContract.KitchenTypesColumns.TABLE_NAME);
        queryConditions.setColumns(new String[]{
                DatabaseContract.KitchenTypesColumns.PRIMEID
        });
        StringBuilder whenCondition = new StringBuilder(DatabaseContract.KitchenTypesColumns.ACTIVE + " = 1");
        if (kitchensId.size() != 0) {
            whenCondition.append(" AND (");
            for (int curKitchenId: kitchensId) {
                whenCondition.append("(").append(DatabaseContract.KitchenTypesColumns.INDEXID).append(" = ").
                        append(curKitchenId).append(")");
                if (curKitchenId != kitchensId.get(kitchensId.size() - 1)) {
                    whenCondition.append(" OR ");
                }
            }
            whenCondition.append(")");
        }
        else return -1;
        queryConditions.setWhereCondition(whenCondition.toString());
        ArrayList<HashMap<String, Object>> data = DatabaseWork.makeData(queryConditions);

        for (HashMap<String, Object> singleType: data) {
            Object cur = singleType.get(DatabaseContract.KitchenTypesColumns.PRIMEID);
            int curPrime = Integer.parseInt(cur.toString());
            result *= curPrime;
        }
        return result;
    }

    public String getKitchensName(int num){
        ArrayList<Integer> primes = prime(num);

        QueryConditions queryConditions = new QueryConditions();
        queryConditions.setTableName(DatabaseContract.KitchenTypesColumns.TABLE_NAME);
        queryConditions.setColumns(new String[]{ DatabaseContract.KitchenTypesColumns.CAPTION });
        StringBuilder whereCondition = new StringBuilder(DatabaseContract.KitchenTypesColumns.ACTIVE + " = 1");
        if (primes.size() > 0) {
            whereCondition.append(" AND (");
            for (int prime: primes) {
                whereCondition.append("(" + DatabaseContract.KitchenTypesColumns.PRIMEID + " = ").append(prime).append(")");
                if (prime != primes.get(primes.size() - 1)) {
                    whereCondition.append(" OR ");
                }
            }
            whereCondition.append(")");
        }
        queryConditions.setWhereCondition(whereCondition.toString());
        ArrayList<HashMap<String, Object>> data = DatabaseWork.makeData(queryConditions);

        if (data.size() == 0) { return ""; }
        StringBuilder result = new StringBuilder();
        HashMap<String, Object> lastType = data.get(data.size() - 1);
        for (HashMap<String, Object> singleType: data) {
            result.append(singleType.get(DatabaseContract.KitchenTypesColumns.CAPTION).toString());
            if (!singleType.equals(lastType)) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    public static ArrayList<String> getKitchenNumbersByNames(ArrayList<String> kitchens){
        ArrayList<String> result = new ArrayList<>();
        for (String kitchen : kitchens){
                    QueryConditions qCond = new QueryConditions();
                    qCond.setTableName(DatabaseContract.KitchenTypesColumns.TABLE_NAME);
                    qCond.setWhereCondition(DatabaseContract.KitchenTypesColumns.CAPTION + " = " + DatabaseWork.prepare(kitchen));
                    qCond.setColumns(new String[] {DatabaseContract.KitchenTypesColumns.PRIMEID});
                    result.add(DatabaseWork.makeData(qCond).get(0).get(DatabaseContract.KitchenTypesColumns.PRIMEID).toString());
        }
        return result;
    }

}
