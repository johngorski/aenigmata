# String#ord gives the ASCII value of a given character
# Number#chr gives the character for the given value

def az_index c
    c.upcase.ord - 'A'.ord
end

def i_char i
    ('A'.ord + i).chr
end

def shift c, i
    i_char((az_index(c) + i) % 26)
end

# (0..25).each { |i| print i_char i }
# puts
# ('A'..'Z').each { |c| print az_index c }
# puts
# (0..25).each { |i| print shift 'X', i }
# puts

ciphertext = 'SSOJUFWFFSFBLJYMNHMMAIHXVTGCYNDXYVBTFHYYK'
ss_raw =     '02112111111111112121111112111111211121111'
shiftshifts = ss_raw.split(//).map {|r| r.ord - '0'.ord}

# Example puzzle
# ciphertext = 'GMIZYFXZRA'
# shiftshifts = [0, 2, 1, 1, 2, 2, 1, 2, 1, 2]

puts "ciphertext doesn't match shiftshifts (missing #{ciphertext.length - shiftshifts.length} shiftshifts)" unless ciphertext.length == shiftshifts.length

(0...10).each do |initial_cumulative_shift|
    cum_shift = initial_cumulative_shift
    (0...ciphertext.length).each do |i|
        cum_shift += shiftshifts[i]
        print shift ciphertext[i], cum_shift % 10
    end
    puts
end
