#include <iostream>
#include <string>
#include <vector>
#include <random>
#include <cmath>
#include <algorithm>
#include <chrono>

using namespace std;

// 辅助函数：检查一个数是否为素数
bool isPrime(int n) {
    if (n <= 1) return false;
    if (n <= 3) return true;
    if (n % 2 == 0 || n % 3 == 0) return false;
    for (int i = 5; i * i <= n; i += 6) {
        if (n % i == 0 || n % (i + 2) == 0) {
            return false;
        }
    }
    return true;
}

// 辅助函数：生成随机素数
int generateRandomPrime(int min, int max) {
    mt19937 gen(chrono::system_clock::now().time_since_epoch().count());
    uniform_int_distribution<> dis(min, max);
    int num;
    do {
        num = dis(gen);
        cout<<"num: "<<num<<endl;
    } while (!isPrime(num));
    
    return num;
}

int main(){
  int p = generateRandomPrime(100, 2000);
  int q = generateRandomPrime(1500, 3500); 
  cout<<"p: "<<p<<",q: "<<q<<endl;
}