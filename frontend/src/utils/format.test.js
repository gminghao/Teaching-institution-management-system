import { describe, expect, it } from 'vitest'
import {
  enrollmentStatusMap,
  enrollmentStatusTone,
  paymentStatusMap,
  paymentStatusTone
} from './format'

describe('status formatting helpers', () => {
  it('maps enrollment status values to Chinese labels and visual tones', () => {
    expect(enrollmentStatusMap.ENROLLED).toBe('已报名')
    expect(enrollmentStatusMap.PENDING).toBe('待处理')
    expect(enrollmentStatusTone('ENROLLED')).toBe('success')
    expect(enrollmentStatusTone('CONTACTED')).toBe('warning')
    expect(enrollmentStatusTone('CANCELLED')).toBe('danger')
    expect(enrollmentStatusTone('UNKNOWN')).toBe('info')
  })

  it('maps payment status values to Chinese labels and payment-specific tones', () => {
    expect(paymentStatusMap.PAID).toBe('已缴费')
    expect(paymentStatusMap.UNPAID).toBe('未缴费')
    expect(paymentStatusTone('PAID')).toBe('success')
    expect(paymentStatusTone('PARTIAL')).toBe('warning')
    expect(paymentStatusTone('UNPAID')).toBe('danger')
    expect(paymentStatusTone('REFUNDED')).toBe('info')
    expect(paymentStatusTone('UNKNOWN')).toBe('info')
  })
})
