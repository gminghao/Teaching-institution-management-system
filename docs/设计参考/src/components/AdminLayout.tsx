import { Link, Outlet } from "react-router-dom";
import {
  LayoutDashboard,
  Users,
  CreditCard,
  GraduationCap,
  Settings,
  LogOut,
  Bell,
  HelpCircle,
  Search
} from "lucide-react";

export default function AdminLayout() {
  return (
    <div className="bg-background text-foreground min-h-screen flex font-sans">
      <aside className="fixed left-0 top-0 h-screen w-64 bg-slate-900 shadow-xl flex flex-col py-8 z-40">
        <div className="px-6 mb-8 flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-primary flex items-center justify-center">
            <GraduationCap className="text-white w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-white">Admin Portal</h1>
            <p className="text-sm text-slate-400">Institutional Access</p>
          </div>
        </div>
        
        <nav className="flex-1 px-3 flex flex-col gap-2 relative">
          {/* Mock routes for UI visualization */}
          <Link to="/admin" className="flex items-center gap-3 px-4 py-3 text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg mx-2 transition-all">
            <LayoutDashboard className="w-5 h-5" />
            <span className="font-medium text-sm">控制台</span>
          </Link>
          <Link to="/admin" className="flex items-center gap-3 px-4 py-3 text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg mx-2 transition-all">
            <GraduationCap className="w-5 h-5" />
            <span className="font- medium text-sm">课程管理</span>
          </Link>
          <Link to="/admin" className="flex items-center gap-3 px-4 py-3 bg-primary-container text-white rounded-lg mx-2 transition-all shadow-md">
            <CreditCard className="w-5 h-5" />
            <span className="font-medium text-sm">财务管理</span>
          </Link>
          <Link to="/admin" className="flex items-center gap-3 px-4 py-3 text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg mx-2 transition-all">
            <Users className="w-5 h-5" />
            <span className="font-medium text-sm">报名管理</span>
          </Link>
        </nav>
        
        <div className="mt-auto px-3 flex flex-col gap-2 pt-6 border-t border-slate-800">
          <Link to="/" className="flex items-center gap-3 px-4 py-3 text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg mx-2 transition-all">
            <LogOut className="w-5 h-5" />
            <span className="font-medium text-sm">退出返回前台</span>
          </Link>
        </div>
      </aside>

      <main className="flex-1 ml-64 flex flex-col min-h-screen">
        <header className="bg-surface border-b border-border h-16 flex items-center justify-between px-8 sticky top-0 z-30">
          <h2 className="text-2xl font-bold text-foreground">财务管理</h2>
          
          <div className="flex items-center gap-4">
            <div className="flex items-center bg-surface-dim rounded-lg px-3 py-1.5 border border-border w-64">
              <Search className="w-4 h-4 text-muted mr-2" />
              <input 
                type="text" 
                placeholder="搜索..." 
                className="bg-transparent border-none focus:ring-0 text-sm p-0 w-full outline-none"
              />
            </div>
            <button className="text-muted hover:text-foreground">
              <Bell className="w-5 h-5" />
            </button>
            <button className="text-muted hover:text-foreground">
              <HelpCircle className="w-5 h-5" />
            </button>
            <div className="w-8 h-8 rounded-full overflow-hidden border border-border ml-2 bg-primary">
              <img 
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuCo1B7YjWylVMkaR_PRsed9_gR7bgKMq7L7Fo1QUHgDo3lUJt3Yz61hdRpbP4lUxrqXex5BxKE1Q9Edh4bMpoe7o_st2RtCXLOytw5FHcjiAfTnm08mHBji7j7BEeq5Vgcxpzneh8uX19I8bmgKlmDbEuAn_upIg9KEkOYI3RwZ-RGwIHyJGyIuX-gWqnLQ_NFtV9fEkeP8qKxVjPHxcc81_OUd4Lk3aOikCB1RcffBdypxNvOEsfB128Lq26hx5sBWavefpwDrBAY0" 
                alt="Admin Profile" 
                className="w-full h-full object-cover"
              />
            </div>
          </div>
        </header>

        <div className="flex-1 overflow-x-hidden bg-background">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
