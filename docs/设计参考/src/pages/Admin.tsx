import { Download, Plus, Search, TrendingUp, CheckCircle, Clock } from "lucide-react";
import { mockTransactions } from "../data/mock";
import { cn } from "../lib/utils";

export default function Admin() {
  return (
    <div className="p-8 max-w-7xl mx-auto flex flex-col gap-8 w-full pb-20">
      
      {/* Header */}
      <div className="flex justify-between items-end">
        <div>
          <h3 className="text-2xl font-bold text-foreground mb-1">财务概览</h3>
          <p className="text-sm text-muted">本月实时财务数据更新至 2026年06月11日</p>
        </div>
        <div className="flex gap-3">
          <button className="px-4 py-2 bg-surface border border-border text-foreground rounded-lg hover:bg-surface-dim transition-colors text-sm font-medium flex items-center gap-2">
            <Download className="w-4 h-4" /> 导出报表
          </button>
          <button className="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors shadow-sm text-sm font-medium flex items-center gap-2">
            <Plus className="w-4 h-4" /> 登记缴费
          </button>
        </div>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-surface border border-border rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex justify-between items-start mb-4">
            <p className="text-xs font-semibold text-muted uppercase">总营收 (Total Revenue)</p>
          </div>
          <h4 className="text-3xl font-bold text-primary mb-2">¥1,245,000</h4>
          <div className="flex items-center gap-1 text-success font-medium text-xs">
            <TrendingUp className="w-4 h-4" /> +12.5% 较上月
          </div>
        </div>
        <div className="bg-surface border border-border rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex justify-between items-start mb-4">
            <p className="text-xs font-semibold text-muted uppercase">报名费 (Registration Fees)</p>
          </div>
          <h4 className="text-3xl font-bold text-foreground mb-2">¥850,000</h4>
          <div className="flex items-center gap-1 text-success font-medium text-xs">
            <TrendingUp className="w-4 h-4" /> +5.2% 较上月
          </div>
        </div>
        <div className="bg-surface border border-border rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex justify-between items-start mb-4">
            <p className="text-xs font-semibold text-muted uppercase">已缴金额 (Paid Amount)</p>
          </div>
          <h4 className="text-3xl font-bold text-foreground mb-2">¥1,100,000</h4>
          <div className="flex items-center gap-1 text-muted font-medium text-xs">
            88% 收款率
          </div>
        </div>
        <div className="bg-surface border border-border rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex justify-between items-start mb-4">
            <p className="text-xs font-semibold text-muted uppercase">待缴余额 (Outstanding)</p>
          </div>
          <h4 className="text-3xl font-bold text-warning mb-2">¥145,000</h4>
          <div className="flex items-center gap-1 text-muted font-medium text-xs">
            125 名学员未缴清
          </div>
        </div>
      </div>

      {/* Middle Grid: Charts & Summary */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Trend Bar Chart Placeholder */}
        <div className="lg:col-span-2 bg-surface border border-border rounded-xl p-6 shadow-sm">
          <h3 className="text-base font-bold text-foreground mb-4">营收趋势 (Revenue Trend)</h3>
          <div className="h-64 relative flex items-end justify-between px-4 border-b border-border pb-2">
            <div className="absolute left-0 top-0 h-full flex flex-col justify-between text-muted text-xs pb-2 w-12 pt-2">
              <span>¥400k</span>
              <span>¥300k</span>
              <span>¥200k</span>
              <span>¥100k</span>
              <span>¥0</span>
            </div>
            
            <div className="ml-12 flex-1 flex justify-around items-end h-[90%] relative z-10 bottom-2">
              <div className="flex flex-col items-center gap-2 w-1/4 group">
                <div className="w-3/4 bg-primary/30 rounded-t h-[40%] group-hover:bg-primary transition-colors"></div>
                <span className="text-xs text-muted">9月</span>
              </div>
              <div className="flex flex-col items-center gap-2 w-1/4 group">
                <div className="w-3/4 bg-primary/40 rounded-t h-[60%] group-hover:bg-primary transition-colors"></div>
                <span className="text-xs text-muted">10月</span>
              </div>
              <div className="flex flex-col items-center gap-2 w-1/4 group">
                <div className="w-3/4 bg-primary/60 rounded-t h-[85%] group-hover:bg-primary transition-colors"></div>
                <span className="text-xs text-muted">11月</span>
              </div>
              <div className="flex flex-col items-center gap-2 w-1/4 group">
                <div className="w-3/4 bg-primary rounded-t h-[100%]"></div>
                <span className="text-xs font-bold text-foreground">12月</span>
              </div>
            </div>

            {/* Grid Lines */}
            <div className="absolute inset-0 flex flex-col justify-between pointer-events-none pb-2 h-full">
              <div className="w-full border-t border-border/60"></div>
              <div className="w-full border-t border-border/60"></div>
              <div className="w-full border-t border-border/60"></div>
              <div className="w-full border-t border-border/60"></div>
              <div className="w-full h-px"></div>
            </div>
          </div>
        </div>
        
        {/* Recent Payments Summary */}
        <div className="lg:col-span-1 bg-surface border border-border rounded-xl p-6 shadow-sm flex flex-col">
          <div className="flex justify-between items-center mb-6">
             <h3 className="text-base font-bold text-foreground">最近缴费记录</h3>
             <a href="#" className="text-sm font-medium text-primary hover:underline">查看全部</a>
          </div>
          <div className="flex-1 flex flex-col gap-1 overflow-y-auto">
            {mockTransactions.slice(0,3).map(tx => (
               <div key={tx.id} className="flex justify-between items-center py-3 border-b border-border/50 last:border-0 hover:bg-surface-dim transition-colors -mx-4 px-4 rounded-lg">
                 <div className="flex items-center gap-3">
                   <div className="w-10 h-10 rounded-full bg-surface-dim flex items-center justify-center text-primary font-bold text-sm">
                     {tx.name[0]}
                   </div>
                   <div>
                     <p className="text-sm font-medium text-foreground">{tx.name}</p>
                     <p className="text-xs text-muted">{tx.method}</p>
                   </div>
                 </div>
                 <div className="text-right">
                    <p className="text-sm font-bold text-foreground">+{tx.amount}</p>
                    <p className={cn("text-xs font-medium", tx.status === "completed" ? "text-success" : tx.status === "pending" ? "text-warning" : "text-error")}>
                      {tx.status === "completed" ? "成功" : tx.status === "pending" ? "处理中" : "失败"}
                    </p>
                 </div>
               </div>
            ))}
          </div>
        </div>

      </div>

      {/* Transaction Table */}
      <div className="bg-surface border border-border rounded-xl shadow-sm overflow-hidden">
        <div className="p-5 border-b border-border bg-surface flex justify-between items-center">
          <h3 className="text-base font-bold text-foreground">缴费明细流水</h3>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-muted w-4 h-4" />
            <input 
              type="text" 
              placeholder="搜索订单号或姓名..." 
              className="pl-9 pr-4 py-2 border border-border rounded-lg text-sm focus:border-primary focus:ring-1 focus:ring-primary w-64 bg-surface outline-none"
            />
          </div>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-dim border-b border-border">
                <th className="py-3 px-6 text-xs font-medium text-muted">日期 (Date)</th>
                <th className="py-3 px-6 text-xs font-medium text-muted">订单号 (Order ID)</th>
                <th className="py-3 px-6 text-xs font-medium text-muted">学员姓名 (Payer)</th>
                <th className="py-3 px-6 text-xs font-medium text-muted text-right">金额 (Amount)</th>
                <th className="py-3 px-6 text-xs font-medium text-muted">支付方式 (Method)</th>
                <th className="py-3 px-6 text-xs font-medium text-muted">状态 (Status)</th>
                <th className="py-3 px-6 text-xs font-medium text-muted">经办人 (Operator)</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {mockTransactions.map(tx => (
                <tr key={tx.id} className="hover:bg-surface-dim transition-colors">
                  <td className="py-4 px-6 text-sm text-foreground whitespace-nowrap">{tx.date}</td>
                  <td className="py-4 px-6 text-sm font-medium text-primary">{tx.id}</td>
                  <td className="py-4 px-6 text-sm text-foreground">{tx.name}</td>
                  <td className="py-4 px-6 text-sm font-bold text-foreground text-right">{tx.amount}</td>
                  <td className="py-4 px-6 text-sm text-muted">{tx.method}</td>
                  <td className="py-4 px-6">
                    <span className={cn(
                      "inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border",
                      tx.status === "completed" 
                        ? "bg-[rgba(16,185,129,0.1)] text-success border-[rgba(16,185,129,0.2)]" 
                        : tx.status === "pending"
                        ? "bg-[rgba(245,158,11,0.1)] text-warning border-[rgba(245,158,11,0.2)]"
                        : "bg-[rgba(239,68,68,0.1)] text-error border-[rgba(239,68,68,0.2)]"
                    )}>
                      {tx.status === "completed" ? "已完成" : tx.status === "pending" ? "处理中" : "失败"}
                    </span>
                  </td>
                  <td className="py-4 px-6 text-sm text-muted">{tx.operator}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        
        <div className="p-4 border-t border-border flex justify-between items-center bg-surface-dim">
           <span className="text-sm text-muted">显示 1-4 条，共 1,248 条记录</span>
           <div className="flex gap-1">
             <button className="w-8 h-8 flex items-center justify-center border border-border rounded bg-surface text-muted disabled:opacity-50" disabled>&lt;</button>
             <button className="w-8 h-8 flex items-center justify-center border border-primary rounded bg-primary text-white text-sm font-medium">1</button>
             <button className="w-8 h-8 flex items-center justify-center border border-border rounded bg-surface text-foreground text-sm font-medium hover:bg-surface-dim">2</button>
             <button className="w-8 h-8 flex items-center justify-center border border-border rounded bg-surface text-foreground text-sm font-medium hover:bg-surface-dim">3</button>
             <span className="w-8 h-8 flex items-center justify-center text-muted">...</span>
             <button className="w-8 h-8 flex items-center justify-center border border-border rounded bg-surface text-muted hover:bg-surface-dim">&gt;</button>
           </div>
        </div>
      </div>
    </div>
  );
}
